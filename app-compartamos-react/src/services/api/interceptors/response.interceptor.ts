import axios, { AxiosError, AxiosInstance, AxiosResponse } from 'axios';
import { tokenStorage } from '../storage';
import { ApiError, AuthExpiredCallback, CustomAxiosRequestConfig } from '../types';

let isRefreshing = false;
let failedQueue: {
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}[] = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

/**
 * Normaliza cualquier error de Axios o de Red en un objeto ApiError legible y predecible.
 */
export const normalizeError = (error: AxiosError): ApiError => {
  if (!error.response) {
    return {
      message: error.message || 'No se pudo conectar con el servidor. Revisa tu conexión a internet.',
      isNetworkError: true,
      code: error.code,
    };
  }

  const responseData = error.response.data as Record<string, unknown> | undefined;
  const status = error.response.status;

  let message = 'Ocurrió un error en la solicitud.';
  if (responseData && typeof responseData === 'object') {
    if (typeof responseData.message === 'string') {
      message = responseData.message;
    } else if (typeof responseData.error === 'string') {
      message = responseData.error;
    }
  } else if (typeof error.response.data === 'string') {
    message = error.response.data;
  }

  if (status === 401) {
    message = message || 'Sesión expirada o no autorizada.';
  } else if (status === 403) {
    message = message || 'No tienes permisos para realizar esta acción.';
  } else if (status === 404) {
    message = message || 'Recurso no encontrado.';
  } else if (status >= 500) {
    message = message || 'Error interno en el servidor. Inténtalo más tarde.';
  }

  return {
    status,
    message,
    data: error.response.data,
    code: error.code,
    isNetworkError: false,
  };
};

/**
 * Configura el interceptor de respuestas (Response Interceptor).
 * - Procesa respuestas exitosas.
 * - Maneja errores de conexión y respuestas HTTP.
 * - Gestiona el refresco automático de token (401) con cola de peticiones concurrentes.
 * - Invoca un callback de sesión expirada para redirigir a Login si el token caduca.
 */
export const setupResponseInterceptor = (
  axiosInstance: AxiosInstance,
  onUnauthorized?: AuthExpiredCallback
): void => {
  axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => {
      if (__DEV__) {
        console.log(
          `✅ [HTTP RESPONSE] [${response.config.method?.toUpperCase()}] ${response.config.url} (${response.status})`,
          {
            data: response.data,
          }
        );
      }
      return response;
    },
    async (error: AxiosError) => {
      const originalRequest = error.config as (CustomAxiosRequestConfig & { _retry?: boolean }) | undefined;

      if (__DEV__) {
        console.error(
          `❌ [HTTP RESPONSE ERROR] [${originalRequest?.method?.toUpperCase()}] ${originalRequest?.url}`,
          {
            status: error.response?.status,
            data: error.response?.data,
            message: error.message,
          }
        );
      }

      // Manejo específico del error 401 (No autorizado / Token expirado)
      if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
        // Evitar reintentar la ruta de autenticación o login si dio 401
        if (originalRequest.url?.includes('/auth/signIn') || originalRequest.url?.includes('/auth/refresh')) {
          return Promise.reject(normalizeError(error));
        }

        const refreshToken = await tokenStorage.getRefreshToken();

        // Si no hay refresh token, cerrar sesión directamente
        if (!refreshToken) {
          await tokenStorage.clearTokens();
          if (onUnauthorized) {
            await onUnauthorized();
          }
          return Promise.reject(normalizeError(error));
        }

        if (isRefreshing) {
          // Si ya hay un refresco en progreso, encolar esta petición
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          })
            .then((token) => {
              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${token}`;
              }
              return axiosInstance(originalRequest);
            })
            .catch((err) => Promise.reject(normalizeError(err)));
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          // Intentar refrescar el token contra el backend
          // Usamos una instancia limpia de axios sin interceptores para no causar ciclos
          const response = await axios.post<{ token: string; refreshToken?: string }>(
            `${axiosInstance.defaults.baseURL || ''}/api/v1/auth/refresh`,
            { refreshToken }
          );

          const newToken = response.data.token;
          const newRefreshToken = response.data.refreshToken || refreshToken;

          await tokenStorage.setTokens({
            token: newToken,
            refreshToken: newRefreshToken,
          });

          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
          }

          processQueue(null, newToken);
          return axiosInstance(originalRequest);
        } catch (refreshError) {
          processQueue(refreshError, null);
          await tokenStorage.clearTokens();
          if (onUnauthorized) {
            await onUnauthorized();
          }
          return Promise.reject(normalizeError(error));
        } finally {
          isRefreshing = false;
        }
      }

      return Promise.reject(normalizeError(error));
    }
  );
};

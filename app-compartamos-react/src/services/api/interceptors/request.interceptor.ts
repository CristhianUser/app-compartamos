import { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { tokenStorage } from '../storage';
import { CustomAxiosRequestConfig } from '../types';

/**
 * Configura el interceptor de peticiones (Request Interceptor).
 * - Agrega headers globales.
 * - Inyecta el token Bearer JWT desde el almacenamiento seguro si está presente.
 * - Registra logs en consola en modo de desarrollo (__DEV__).
 */
export const setupRequestInterceptor = (axiosInstance: AxiosInstance): void => {
  axiosInstance.interceptors.request.use(
    async (config: InternalAxiosRequestConfig & CustomAxiosRequestConfig) => {
      // Headers por defecto
      config.headers = config.headers || {};
      if (!config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/json';
      }
      if (!config.headers['Accept']) {
        config.headers['Accept'] = 'application/json';
      }

      // Inyección automática del Bearer Token si la petición no lo excluye explícitamente
      if (!config.skipAuth) {
        const token = await tokenStorage.getAccessToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
      }

      // Logging en entorno de desarrollo para fácil depuración
      if (__DEV__) {
        console.log(
          `🌐 [HTTP REQUEST] [${config.method?.toUpperCase()}] ${config.baseURL || ''}${config.url}`,
          {
            headers: config.headers,
            params: config.params,
            data: config.data,
          }
        );
      }

      return config;
    },
    (error) => {
      if (__DEV__) {
        console.error('❌ [HTTP REQUEST ERROR]', error);
      }
      return Promise.reject(error);
    }
  );
};

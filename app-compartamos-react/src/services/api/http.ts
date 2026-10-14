import axios, { AxiosInstance } from 'axios';
import { Platform } from 'react-native';
import { setupRequestInterceptor } from './interceptors/request.interceptor';
import { setupResponseInterceptor } from './interceptors/response.interceptor';
import { AuthExpiredCallback, CustomAxiosRequestConfig } from './types';

// Determina la URL base según la plataforma y variables de entorno de Expo
const getInitialBaseUrl = (): string => {
  if (process.env.EXPO_PUBLIC_API_URL) {
    return process.env.EXPO_PUBLIC_API_URL;
  }
  // En Android Emulator, 10.0.2.2 apunta al localhost de la máquina host
  if (Platform.OS === 'android') {
    return 'http://10.0.2.2:8080';
  }
  // iOS Simulator y Web usan localhost
  return 'http://localhost:8080';
};

/**
 * Instancia principal de Axios configurada para la aplicación
 */
// eslint-disable-next-line import/no-named-as-default-member
export const http: AxiosInstance = axios.create({
  baseURL: getInitialBaseUrl(),
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

let unauthorizedHandler: AuthExpiredCallback | undefined;

/**
 * Configura un manejador para cuando una sesión expira o es inválida (401 recurrente).
 * Permite redirigir al login o limpiar estados globales de React.
 */
export const setUnauthorizedHandler = (handler: AuthExpiredCallback): void => {
  unauthorizedHandler = handler;
};

// Registramos los interceptores de Request y Response
setupRequestInterceptor(http);
setupResponseInterceptor(http, async () => {
  if (unauthorizedHandler) {
    await unauthorizedHandler();
  }
});

/**
 * Métodos auxiliares fuertemente tipados
 */
export const apiClient = {
  get: <T = unknown>(url: string, config?: CustomAxiosRequestConfig) =>
    http.get<T>(url, config).then((res) => res.data),

  post: <T = unknown>(url: string, data?: unknown, config?: CustomAxiosRequestConfig) =>
    http.post<T>(url, data, config).then((res) => res.data),

  put: <T = unknown>(url: string, data?: unknown, config?: CustomAxiosRequestConfig) =>
    http.put<T>(url, data, config).then((res) => res.data),

  delete: <T = unknown>(url: string, config?: CustomAxiosRequestConfig) =>
    http.delete<T>(url, config).then((res) => res.data),

  patch: <T = unknown>(url: string, data?: unknown, config?: CustomAxiosRequestConfig) =>
    http.patch<T>(url, data, config).then((res) => res.data),

  /**
   * Permite actualizar dinámicamente la baseURL en tiempo de ejecución
   */
  setBaseUrl: (newBaseUrl: string) => {
    http.defaults.baseURL = newBaseUrl;
  },

  /**
   * Acceso directo a la instancia de axios
   */
  instance: http,
};

export default http;

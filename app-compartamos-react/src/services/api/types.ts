import { AxiosRequestConfig } from 'axios';

export interface AuthTokens {
  token: string;
  refreshToken?: string;
}

export interface CustomAxiosRequestConfig extends AxiosRequestConfig {
  /**
   * Si es true, el interceptor de request no agregará el header Authorization Bearer.
   * Útil para endpoints públicos como login o registro.
   */
  skipAuth?: boolean;
  /**
   * Marca interna para evitar bucles infinitos en el refresco de token.
   */
  _retry?: boolean;
}

export interface ApiError {
  status?: number;
  message: string;
  data?: unknown;
  code?: string;
  isNetworkError?: boolean;
}

export interface ApiResponse<T = unknown> {
  data: T;
  message?: string;
  success?: boolean;
}

export type AuthExpiredCallback = () => void | Promise<void>;

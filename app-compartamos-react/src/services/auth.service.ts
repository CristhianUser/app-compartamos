import { apiClient, tokenStorage } from './api';

export interface SigninCredentials {
  email: string;
  password: string;
}

export interface SignupData {
  numDoc: string;
  email: string;
  password: string;
}

export interface SigninResponse {
  token: string;
  refreshToken?: string;
}

export interface Usuario {
  id?: number;
  nombres?: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  numDoc?: string;
  email?: string;
}

export const authService = {
  /**
   * Inicio de sesión.
   * Utiliza skipAuth: true porque este endpoint es público.
   * Almacena automáticamente los tokens recibidos en almacenamiento seguro.
   */
  async signIn(credentials: SigninCredentials): Promise<SigninResponse> {
    const data = await apiClient.post<SigninResponse>(
      '/api/v1/auth/signIn',
      credentials,
      { skipAuth: true }
    );

    if (data.token) {
      await tokenStorage.setTokens({
        token: data.token,
        refreshToken: data.refreshToken,
      });
    }

    return data;
  },

  /**
   * Registro de usuario común.
   */
  async signUser(data: SignupData): Promise<Usuario> {
    return apiClient.post<Usuario>('/api/v1/auth/signUser', data, { skipAuth: true });
  },

  /**
   * Cierre de sesión: limpia tokens de almacenamiento seguro.
   */
  async signOut(): Promise<void> {
    await tokenStorage.clearTokens();
  },

  /**
   * Verifica si existe un token de sesión guardado.
   */
  async isAuthenticated(): Promise<boolean> {
    const token = await tokenStorage.getAccessToken();
    return !!token;
  },
};

import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';
import { AuthTokens } from './types';

const ACCESS_TOKEN_KEY = 'auth_access_token';
const REFRESH_TOKEN_KEY = 'auth_refresh_token';

/**
 * Almacenamiento seguro para tokens de autenticación.
 * Utiliza SecureStore en iOS/Android y fallback a localStorage en entornos web.
 */
export const tokenStorage = {
  async getAccessToken(): Promise<string | null> {
    try {
      if (Platform.OS === 'web') {
        if (typeof window !== 'undefined' && window.localStorage) {
          return window.localStorage.getItem(ACCESS_TOKEN_KEY);
        }
        return null;
      }
      return await SecureStore.getItemAsync(ACCESS_TOKEN_KEY);
    } catch (error) {
      console.error('[tokenStorage] Error al obtener accessToken:', error);
      return null;
    }
  },

  async getRefreshToken(): Promise<string | null> {
    try {
      if (Platform.OS === 'web') {
        if (typeof window !== 'undefined' && window.localStorage) {
          return window.localStorage.getItem(REFRESH_TOKEN_KEY);
        }
        return null;
      }
      return await SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
    } catch (error) {
      console.error('[tokenStorage] Error al obtener refreshToken:', error);
      return null;
    }
  },

  async setTokens(tokens: AuthTokens): Promise<void> {
    try {
      if (Platform.OS === 'web') {
        if (typeof window !== 'undefined' && window.localStorage) {
          window.localStorage.setItem(ACCESS_TOKEN_KEY, tokens.token);
          if (tokens.refreshToken) {
            window.localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
          }
        }
        return;
      }

      await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, tokens.token);
      if (tokens.refreshToken) {
        await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, tokens.refreshToken);
      }
    } catch (error) {
      console.error('[tokenStorage] Error al guardar tokens:', error);
    }
  },

  async clearTokens(): Promise<void> {
    try {
      if (Platform.OS === 'web') {
        if (typeof window !== 'undefined' && window.localStorage) {
          window.localStorage.removeItem(ACCESS_TOKEN_KEY);
          window.localStorage.removeItem(REFRESH_TOKEN_KEY);
        }
        return;
      }

      await SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY);
      await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
    } catch (error) {
      console.error('[tokenStorage] Error al limpiar tokens:', error);
    }
  },
};

import { create } from 'zustand';
import * as userApi from '../api/userApi';

const useAuthStore = create((set, get) => ({
  user: JSON.parse(localStorage.getItem('fc_user') || 'null'),
  token: localStorage.getItem('fc_token') || null,
  isAuthenticated: !!localStorage.getItem('fc_token'),
  isLoading: false,
  error: null,

  login: async (email, password) => {
    set({ isLoading: true, error: null });
    try {
      const response = await userApi.login(email, password);
      const token = response.token;
      const user = response.user || {
        id: response.userId,
        nom: response.nom,
        email: response.email,
        role: response.role,
      };
      localStorage.setItem('fc_token', token);
      localStorage.setItem('fc_user', JSON.stringify(user));
      set({
        user,
        token,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
      return user;
    } catch (error) {
      const message =
        error.response?.data?.message ||
        error.response?.data?.error ||
        'Identifiants incorrects';
      set({ isLoading: false, error: message });
      throw new Error(message);
    }
  },

  logout: () => {
    localStorage.removeItem('fc_token');
    localStorage.removeItem('fc_user');
    set({
      user: null,
      token: null,
      isAuthenticated: false,
      error: null,
    });
  },

  setUser: (user) => {
    localStorage.setItem('fc_user', JSON.stringify(user));
    set({ user });
  },

  clearError: () => set({ error: null }),
}));

export default useAuthStore;

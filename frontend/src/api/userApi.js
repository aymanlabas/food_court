import api from './axiosInstance';

// --- Auth ---
export const login = (email, password) =>
  api.post('/auth/login', { email, password }).then((r) => r.data);

export const register = (data) =>
  api.post('/auth/register', data).then((r) => r.data);

// --- Users ---
export const getUsers = () => api.get('/users').then((r) => r.data);

export const getUser = (id) => api.get(`/users/${id}`).then((r) => r.data);

export const getCurrentUser = () => api.get('/users/me').then((r) => r.data);

export const updateUser = (id, data) => api.put(`/users/${id}`, data).then((r) => r.data);

export const deleteUser = (id) => api.delete(`/users/${id}`).then((r) => r.data);

export const updateBalance = (id, data) =>
  api.patch(`/users/${id}/balance`, data).then((r) => r.data);

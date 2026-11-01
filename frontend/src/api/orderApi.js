import api from './axiosInstance';

export const createOrder = (data) =>
  api.post('/orders', data).then((r) => r.data);

export const getOrders = () =>
  api.get('/orders').then((r) => r.data);

export const getOrder = (id) =>
  api.get(`/orders/${id}`).then((r) => r.data);

export const getUserOrders = (userId) =>
  api.get(`/orders/user/${userId}`).then((r) => r.data);

export const getKitchenOrders = () =>
  api.get('/orders/kitchen').then((r) => r.data);

export const getOrderStats = () =>
  api.get('/orders/stats').then((r) => r.data);

export const updateOrderStatus = (id, statut) =>
  api.patch(`/orders/${id}/status`, { statut }).then((r) => r.data);

export const cancelOrder = (id) =>
  api.post(`/orders/${id}/cancel`).then((r) => r.data);


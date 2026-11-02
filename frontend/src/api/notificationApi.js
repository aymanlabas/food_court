import api from './axiosInstance';

export const getUserNotifications = (userId) =>
  api.get(`/notifications/user/${userId}`).then((r) => r.data);

export const getUnreadNotifications = (userId) =>
  api.get(`/notifications/user/${userId}/unread`).then((r) => r.data);

export const getUnreadCount = (userId) =>
  api.get(`/notifications/user/${userId}/count`).then((r) => r.data);

export const markAsRead = (id) =>
  api.patch(`/notifications/${id}/read`).then((r) => r.data);

export const markAllAsRead = (userId) =>
  api.patch(`/notifications/user/${userId}/read-all`).then((r) => r.data);


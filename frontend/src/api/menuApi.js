import api from './axiosInstance';

// --- Dishes ---
export const getDishes = (params) => api.get('/dishes', { params }).then((r) => r.data);

export const getDish = (id) => api.get(`/dishes/${id}`).then((r) => r.data);

export const createDish = (data) => api.post('/dishes', data).then((r) => r.data);

export const updateDish = (id, data) => api.put(`/dishes/${id}`, data).then((r) => r.data);

export const deleteDish = (id) => api.delete(`/dishes/${id}`).then((r) => r.data);

export const getAvailableDishes = () => api.get('/dishes/available').then((r) => r.data);

export const toggleAvailability = (id) => api.patch(`/dishes/${id}/availability`).then((r) => r.data);

// --- Ingredients ---
export const getIngredients = () => api.get('/ingredients').then((r) => r.data);

export const getIngredient = (id) => api.get(`/ingredients/${id}`).then((r) => r.data);

export const createIngredient = (data) => api.post('/ingredients', data).then((r) => r.data);

export const updateIngredient = (id, data) => api.put(`/ingredients/${id}`, data).then((r) => r.data);

export const deleteIngredient = (id) => api.delete(`/ingredients/${id}`).then((r) => r.data);

export const getLowStockIngredients = () => api.get('/ingredients/low-stock').then((r) => r.data);

export const updateStock = (id, data) => api.patch(`/ingredients/${id}/stock`, data).then((r) => r.data);

// --- Recipe Lines ---
export const getRecipeLines = (dishId) => api.get(`/recipe-lines/dish/${dishId}`).then((r) => r.data);

export const createRecipeLine = (data) => api.post('/recipe-lines', data).then((r) => r.data);

export const updateRecipeLine = (id, data) => api.put(`/recipe-lines/${id}`, data).then((r) => r.data);

export const deleteRecipeLine = (id) => api.delete(`/recipe-lines/${id}`).then((r) => r.data);

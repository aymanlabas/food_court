import { create } from 'zustand';

const useCartStore = create((set, get) => ({
  items: [],

  addItem: (dish) => {
    const { items } = get();
    const existing = items.find((item) => item.dish.id === dish.id);
    if (existing) {
      set({
        items: items.map((item) =>
          item.dish.id === dish.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        ),
      });
    } else {
      set({ items: [...items, { dish, quantity: 1 }] });
    }
  },

  removeItem: (dishId) => {
    set({ items: get().items.filter((item) => item.dish.id !== dishId) });
  },

  updateQuantity: (dishId, qty) => {
    if (qty <= 0) {
      set({ items: get().items.filter((item) => item.dish.id !== dishId) });
      return;
    }
    set({
      items: get().items.map((item) =>
        item.dish.id === dishId ? { ...item, quantity: qty } : item
      ),
    });
  },

  clearCart: () => set({ items: [] }),

  getTotal: () => {
    return get().items.reduce(
      (total, item) => total + (item.dish.prix || 0) * item.quantity,
      0
    );
  },

  getItemCount: () => {
    return get().items.reduce((count, item) => count + item.quantity, 0);
  },
}));

export default useCartStore;

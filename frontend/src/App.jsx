import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/common/Layout';
import ProtectedRoute from './components/common/ProtectedRoute';

import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/employee/DashboardPage';
import MenuPage from './pages/employee/MenuPage';
import CartPage from './pages/employee/CartPage';
import OrderHistoryPage from './pages/employee/OrderHistoryPage';
import OrderTrackingPage from './pages/employee/OrderTrackingPage';
import KitchenDashboardPage from './pages/kitchen/KitchenDashboardPage';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import DishManagementPage from './pages/admin/DishManagementPage';
import IngredientManagementPage from './pages/admin/IngredientManagementPage';
import RecipeManagementPage from './pages/admin/RecipeManagementPage';
import EmployeeManagementPage from './pages/admin/EmployeeManagementPage';
import PaymentManagementPage from './pages/admin/PaymentManagementPage';
import OrderManagementPage from './pages/admin/OrderManagementPage';

export default function App() {
  return (
    <Routes>
      {/* Public route */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/menu" element={<MenuPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/orders" element={<OrderHistoryPage />} />
          <Route path="/orders/:id" element={<OrderTrackingPage />} />

          {/* Kitchen routes */}
          <Route element={<ProtectedRoute allowedRoles={['KITCHEN', 'ADMIN']} />}>
            <Route path="/kitchen" element={<KitchenDashboardPage />} />
          </Route>

          {/* Admin routes */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin" element={<AdminDashboardPage />} />
            <Route path="/admin/dishes" element={<DishManagementPage />} />
            <Route path="/admin/ingredients" element={<IngredientManagementPage />} />
            <Route path="/admin/recipes" element={<RecipeManagementPage />} />
            <Route path="/admin/employees" element={<EmployeeManagementPage />} />
            <Route path="/admin/payments" element={<PaymentManagementPage />} />
            <Route path="/admin/orders" element={<OrderManagementPage />} />
          </Route>
        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}


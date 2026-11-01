import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Box, Container } from '@mui/material';
import Navbar from './Navbar';
import Sidebar from './Sidebar';
import useAuthStore from '../../store/authStore';

export default function Layout() {
  const location = useLocation();
  const { user } = useAuthStore();
  const isAdminRoute = location.pathname.startsWith('/admin') && user?.role === 'ADMIN';

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: '#f8fafc' }}>
      <Navbar />
      <Box sx={{ display: 'flex', flex: 1 }}>
        {isAdminRoute && <Sidebar />}
        <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, sm: 3, md: 4 } }}>
          <Container maxWidth={isAdminRoute ? 'xl' : 'lg'} disableGutters>
            <Outlet />
          </Container>
        </Box>
      </Box>
    </Box>
  );
}


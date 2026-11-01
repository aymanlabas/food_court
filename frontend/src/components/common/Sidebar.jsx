import React from 'react';
import { Link as RouterLink, useLocation } from 'react-router-dom';
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Divider,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import KitchenIcon from '@mui/icons-material/Kitchen';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import PeopleIcon from '@mui/icons-material/People';
import ReceiptIcon from '@mui/icons-material/Receipt';
import PaymentIcon from '@mui/icons-material/Payment';

const adminLinks = [
  { text: 'Tableau de bord', path: '/admin', icon: DashboardIcon },
  { text: 'Gestion des Plats', path: '/admin/dishes', icon: RestaurantIcon },
  { text: 'Ingrédients & Stock', path: '/admin/ingredients', icon: KitchenIcon },
  { text: 'Recettes', path: '/admin/recipes', icon: MenuBookIcon },
  { text: 'Employés & Soldes', path: '/admin/employees', icon: PeopleIcon },
  { text: 'Paiements Cantine', path: '/admin/payments', icon: PaymentIcon },
  { text: 'Suivi Commandes', path: '/admin/orders', icon: ReceiptIcon },
];

export default function Sidebar() {
  const location = useLocation();

  return (
    <Box
      sx={{
        width: 260,
        flexShrink: 0,
        borderRight: '1px solid #f1f5f9',
        bgcolor: '#ffffff',
        display: { xs: 'none', md: 'block' },
        minHeight: 'calc(100vh - 70px)',
        py: 3,
      }}
    >
      <Box sx={{ px: 3, mb: 2 }}>
        <Typography variant="caption" fontWeight={700} color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: '0.08em' }}>
          Administration
        </Typography>
      </Box>
      <List disablePadding>
        {adminLinks.map((item) => {
          const Icon = item.icon;
          const active = location.pathname === item.path;
          return (
            <ListItem key={item.path} disablePadding sx={{ px: 1.5, mb: 0.5 }}>
              <ListItemButton
                component={RouterLink}
                to={item.path}
                sx={{
                  borderRadius: 2.5,
                  py: 1.2,
                  px: 2,
                  bgcolor: active ? '#f0fdf4' : 'transparent',
                  color: active ? '#16a34a' : '#475569',
                  '&:hover': {
                    bgcolor: active ? '#dcfce7' : '#f8fafc',
                  },
                }}
              >
                <ListItemIcon sx={{ minWidth: 38, color: active ? '#16a34a' : '#64748b' }}>
                  <Icon fontSize="small" />
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{
                    fontSize: '0.9rem',
                    fontWeight: active ? 700 : 500,
                  }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>
    </Box>
  );
}


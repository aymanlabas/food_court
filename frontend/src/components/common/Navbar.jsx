import React, { useState } from 'react';
import { useNavigate, Link as RouterLink, useLocation } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Chip,
  Badge,
  Container,
  Popover,
  List,
  ListItem,
  ListItemText,
  Divider,
} from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import NotificationsIcon from '@mui/icons-material/Notifications';
import LogoutIcon from '@mui/icons-material/Logout';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import useAuthStore from '../../store/authStore';
import useCartStore from '../../store/cartStore';
import * as notificationApi from '../../api/notificationApi';
import { formatPrice, formatDate } from '../../utils/formatters';

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();
  const { user, logout } = useAuthStore();
  const { items } = useCartStore();
  const [anchorEl, setAnchorEl] = useState(null);
  const [notifAnchorEl, setNotifAnchorEl] = useState(null);

  // V5: Notifications query with polling
  const { data: notifications = [] } = useQuery({
    queryKey: ['notifications', user?.id],
    queryFn: () => notificationApi.getUserNotifications(user?.id),
    enabled: Boolean(user?.id),
    refetchInterval: 8000,
  });

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markReadMutation = useMutation({
    mutationFn: (id) => notificationApi.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications', user?.id] });
    },
  });

  const markAllReadMutation = useMutation({
    mutationFn: () => notificationApi.markAllAsRead(user?.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications', user?.id] });
    },
  });

  const cartItemCount = items.reduce((acc, item) => acc + item.quantity, 0);

  const handleOpenMenu = (event) => setAnchorEl(event.currentTarget);
  const handleCloseMenu = () => setAnchorEl(null);

  const handleOpenNotif = (event) => setNotifAnchorEl(event.currentTarget);
  const handleCloseNotif = () => setNotifAnchorEl(null);

  const handleLogout = () => {
    handleCloseMenu();
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <AppBar position="sticky" color="inherit" elevation={0} sx={{ borderBottom: '1px solid #f1f5f9', bgcolor: '#ffffff' }}>
      <Container maxWidth="xl">
        <Toolbar disableGutters sx={{ justifyContent: 'space-between', minHeight: 70 }}>
          {/* Logo */}
          <Box
            component={RouterLink}
            to="/dashboard"
            sx={{
              display: 'flex',
              alignItems: 'center',
              textDecoration: 'none',
              color: 'inherit',
              gap: 1.5,
            }}
          >
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: '10px',
                bgcolor: '#16a34a',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
                boxShadow: '0 2px 8px rgba(22, 163, 74, 0.35)',
              }}
            >
              <RestaurantMenuIcon fontSize="small" />
            </Box>
            <Box>
              <Typography variant="h6" fontWeight={800} sx={{ letterSpacing: '-0.02em', color: '#0f172a' }}>
                Food<span style={{ color: '#16a34a' }}>Court</span>
              </Typography>
              <Typography variant="caption" sx={{ color: '#64748b', display: 'block', mt: -0.5 }}>
                Restauration d'Entreprise
              </Typography>
            </Box>
          </Box>

          {/* Navigation links */}
          <Box sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center', gap: 1 }}>
            {user?.role === 'EMPLOYEE' && (
              <>
                <Button
                  component={RouterLink}
                  to="/dashboard"
                  color={isActive('/dashboard') ? 'primary' : 'inherit'}
                  variant={isActive('/dashboard') ? 'text' : 'text'}
                  sx={{ fontWeight: isActive('/dashboard') ? 700 : 500 }}
                >
                  Tableau de bord
                </Button>
                <Button
                  component={RouterLink}
                  to="/menu"
                  color={isActive('/menu') ? 'primary' : 'inherit'}
                  sx={{ fontWeight: isActive('/menu') ? 700 : 500 }}
                >
                  Menu du jour
                </Button>
                <Button
                  component={RouterLink}
                  to="/orders"
                  color={isActive('/orders') ? 'primary' : 'inherit'}
                  sx={{ fontWeight: isActive('/orders') ? 700 : 500 }}
                >
                  Mes commandes
                </Button>
              </>
            )}

            {user?.role === 'KITCHEN' && (
              <Button
                component={RouterLink}
                to="/kitchen"
                color={isActive('/kitchen') ? 'primary' : 'inherit'}
                sx={{ fontWeight: isActive('/kitchen') ? 700 : 500 }}
              >
                Tableau de cuisine
              </Button>
            )}

            {user?.role === 'ADMIN' && (
              <>
                <Button
                  component={RouterLink}
                  to="/admin"
                  color={isActive('/admin') ? 'primary' : 'inherit'}
                  sx={{ fontWeight: isActive('/admin') ? 700 : 500 }}
                >
                  Tableau de bord
                </Button>
                <Button
                  component={RouterLink}
                  to="/admin/dishes"
                  color={isActive('/admin/dishes') ? 'primary' : 'inherit'}
                  sx={{ fontWeight: isActive('/admin/dishes') ? 700 : 500 }}
                >
                  Plats
                </Button>
                <Button
                  component={RouterLink}
                  to="/admin/ingredients"
                  color={isActive('/admin/ingredients') ? 'primary' : 'inherit'}
                  sx={{ fontWeight: isActive('/admin/ingredients') ? 700 : 500 }}
                >
                  Ingrédients
                </Button>
                <Button
                  component={RouterLink}
                  to="/admin/recipes"
                  color={isActive('/admin/recipes') ? 'primary' : 'inherit'}
                  sx={{ fontWeight: isActive('/admin/recipes') ? 700 : 500 }}
                >
                  Recettes
                </Button>
                <Button
                  component={RouterLink}
                  to="/admin/employees"
                  color={isActive('/admin/employees') ? 'primary' : 'inherit'}
                  sx={{ fontWeight: isActive('/admin/employees') ? 700 : 500 }}
                >
                  Employés
                </Button>
                <Button
                  component={RouterLink}
                  to="/admin/orders"
                  color={isActive('/admin/orders') ? 'primary' : 'inherit'}
                  sx={{ fontWeight: isActive('/admin/orders') ? 700 : 500 }}
                >
                  Commandes
                </Button>
              </>
            )}
          </Box>

          {/* Right actions: Balance, Cart, User menu */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {user?.role === 'EMPLOYEE' && (
              <>
                <Chip
                  icon={<AccountBalanceWalletIcon sx={{ fontSize: '1.1rem !important', color: '#15803d !important' }} />}
                  label={`Solde: ${formatPrice(user?.soldeCantine || 0)}`}
                  sx={{
                    bgcolor: '#dcfce7',
                    color: '#15803d',
                    fontWeight: 700,
                    px: 1,
                    py: 2,
                    borderRadius: 3,
                  }}
                />

                <IconButton component={RouterLink} to="/cart" color="inherit">
                  <Badge badgeContent={cartItemCount} color="primary">
                    <ShoppingCartIcon />
                  </Badge>
                </IconButton>
              </>
            )}

            {/* Notification Bell */}
            <IconButton onClick={handleOpenNotif} color="inherit">
              <Badge badgeContent={unreadCount} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <IconButton onClick={handleOpenMenu} sx={{ p: 0.5 }}>
                <Avatar sx={{ bgcolor: '#16a34a', width: 38, height: 38, fontSize: '0.95rem', fontWeight: 600 }}>
                  {user?.nom ? user.nom.charAt(0).toUpperCase() : 'U'}
                </Avatar>
              </IconButton>
              <Box sx={{ display: { xs: 'none', lg: 'block' }, textAlign: 'left' }}>
                <Typography variant="body2" fontWeight={600} color="text.primary">
                  {user?.nom || 'Utilisateur'}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {user?.role}
                </Typography>
              </Box>
            </Box>

            <Menu
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              onClose={handleCloseMenu}
              transformOrigin={{ horizontal: 'right', vertical: 'top' }}
              anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
              PaperProps={{
                elevation: 3,
                sx: { minWidth: 200, mt: 1, borderRadius: 2 },
              }}
            >
              <Box sx={{ px: 2, py: 1.5, borderBottom: '1px solid #f1f5f9' }}>
                <Typography variant="subtitle2" fontWeight={700}>
                  {user?.nom}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {user?.email}
                </Typography>
              </Box>
              <MenuItem onClick={handleLogout} sx={{ color: 'error.main', mt: 1 }}>
                <LogoutIcon fontSize="small" sx={{ mr: 1.5 }} />
                Déconnexion
              </MenuItem>
            </Menu>

            {/* Notifications Popover */}
            <Popover
              anchorEl={notifAnchorEl}
              open={Boolean(notifAnchorEl)}
              onClose={handleCloseNotif}
              anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              transformOrigin={{ vertical: 'top', horizontal: 'right' }}
              PaperProps={{
                elevation: 4,
                sx: { width: 360, maxHeight: 480, borderRadius: 3, mt: 1 },
              }}
            >
              <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9' }}>
                <Typography variant="subtitle1" fontWeight={700}>
                  Notifications
                </Typography>
                {unreadCount > 0 && (
                  <Button
                    size="small"
                    startIcon={<DoneAllIcon />}
                    onClick={() => markAllReadMutation.mutate()}
                    sx={{ fontSize: '0.75rem', textTransform: 'none' }}
                  >
                    Tout lire
                  </Button>
                )}
              </Box>

              {notifications.length === 0 ? (
                <Box sx={{ p: 4, textAlign: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    Aucune notification pour le moment.
                  </Typography>
                </Box>
              ) : (
                <List disablePadding sx={{ maxHeight: 380, overflowY: 'auto' }}>
                  {notifications.map((notif) => (
                    <ListItem
                      key={notif.id}
                      button
                      onClick={() => {
                        if (!notif.read) markReadMutation.mutate(notif.id);
                        handleCloseNotif();
                        if (notif.mealOrderId) {
                          navigate(`/orders/${notif.mealOrderId}`);
                        }
                      }}
                      sx={{
                        px: 2,
                        py: 1.5,
                        borderBottom: '1px solid #f8fafc',
                        bgcolor: notif.read ? 'transparent' : '#f0fdf4',
                        '&:hover': { bgcolor: '#f1f5f9' },
                      }}
                    >
                      <ListItemText
                        primary={
                          <Typography variant="body2" fontWeight={notif.read ? 600 : 700} color={notif.read ? 'text.primary' : '#15803d'}>
                            {notif.title}
                          </Typography>
                        }
                        secondary={
                          <Box sx={{ mt: 0.5 }}>
                            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
                              {notif.message}
                            </Typography>
                            <Typography variant="caption" color="#94a3b8" sx={{ fontSize: '0.7rem' }}>
                              {formatDate(notif.createdAt)}
                            </Typography>
                          </Box>
                        }
                      />
                    </ListItem>
                  ))}
                </List>
              )}
            </Popover>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
}


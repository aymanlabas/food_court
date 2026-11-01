import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Typography,
  Grid,
  Button,
  Card,
  CardContent,
  Chip,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningIcon from '@mui/icons-material/Warning';
import KitchenIcon from '@mui/icons-material/Kitchen';
import PeopleIcon from '@mui/icons-material/People';
import AddIcon from '@mui/icons-material/Add';
import * as menuApi from '../../api/menuApi';
import * as userApi from '../../api/userApi';
import StatsCard from '../../components/common/StatsCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export default function AdminDashboardPage() {
  const { data: dishes = [], isLoading: loadingDishes } = useQuery({
    queryKey: ['dishes'],
    queryFn: () => menuApi.getDishes(),
  });

  const { data: ingredients = [], isLoading: loadingIngredients } = useQuery({
    queryKey: ['ingredients'],
    queryFn: menuApi.getIngredients,
  });

  const { data: lowStockIngredients = [] } = useQuery({
    queryKey: ['lowStockIngredients'],
    queryFn: menuApi.getLowStockIngredients,
  });

  const { data: users = [], isLoading: loadingUsers } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getUsers,
  });

  const availableDishesCount = dishes.filter((d) => d.disponible).length;
  const unavailableDishes = dishes.filter((d) => !d.disponible);

  if (loadingDishes || loadingIngredients || loadingUsers) {
    return <LoadingSpinner message="Chargement des statistiques d'administration..." />;
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header with Quick Actions */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Tableau de Bord Administrateur ⚙️
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Vue d'ensemble des menus, des stocks d'ingrédients et des utilisateurs de la cantine.
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap' }}>
          <Button
            component={RouterLink}
            to="/admin/dishes"
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
          >
            Gérer les Plats
          </Button>
          <Button
            component={RouterLink}
            to="/admin/ingredients"
            variant="outlined"
            color="primary"
            startIcon={<AddIcon />}
          >
            Gérer le Stock
          </Button>
        </Box>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatsCard
            title="Total Plats"
            value={dishes.length}
            subtitle="Plats au catalogue"
            icon={RestaurantIcon}
            color="#3b82f6"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatsCard
            title="Plats Disponibles"
            value={availableDishesCount}
            subtitle="Disponibles aujourd'hui"
            icon={CheckCircleIcon}
            color="#16a34a"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatsCard
            title="Ingrédients"
            value={ingredients.length}
            subtitle="Matières premières"
            icon={KitchenIcon}
            color="#8b5cf6"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatsCard
            title="Alertes Stock Bas"
            value={lowStockIngredients.length}
            subtitle="En dessous du seuil"
            icon={WarningIcon}
            color="#dc2626"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatsCard
            title="Collaborateurs"
            value={users.length}
            subtitle="Comptes enregistrés"
            icon={PeopleIcon}
            color="#f59e0b"
          />
        </Grid>
      </Grid>

      {/* Two columns: Low stock alerts & Unavailable dishes */}
      <Grid container spacing={3}>
        {/* Low Stock Alerts */}
        <Grid item xs={12} lg={6}>
          <Card sx={{ height: '100%', borderRadius: 3, border: '1px solid #f1f5f9' }}>
            <CardContent sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <WarningIcon sx={{ color: '#dc2626' }} />
                  <Typography variant="h6" fontWeight={700}>
                    Alertes Ingrédients en Stock Critique
                  </Typography>
                </Box>
                <Chip
                  label={`${lowStockIngredients.length} alerte(s)`}
                  size="small"
                  color={lowStockIngredients.length > 0 ? 'error' : 'success'}
                  sx={{ fontWeight: 700 }}
                />
              </Box>

              {lowStockIngredients.length === 0 ? (
                <Paper elevation={0} sx={{ p: 4, textAlign: 'center', bgcolor: '#f8fafc', borderRadius: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Tous les stocks d'ingrédients sont au-dessus de leur seuil d'alerte. Bravo ! 🎉
                  </Typography>
                </Paper>
              ) : (
                <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid #f1f5f9', borderRadius: 2 }}>
                  <Table size="small">
                    <TableHead sx={{ bgcolor: '#f8fafc' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 700 }}>Ingrédient</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 700 }}>Stock Actuel</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 700 }}>Seuil d'Alerte</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {lowStockIngredients.map((item) => (
                        <TableRow key={item.id} hover>
                          <TableCell sx={{ fontWeight: 600 }}>{item.nom}</TableCell>
                          <TableCell align="right" sx={{ color: '#dc2626', fontWeight: 700 }}>
                            {item.stock} {item.unite}
                          </TableCell>
                          <TableCell align="right" sx={{ color: '#64748b' }}>
                            {item.seuilAlerte} {item.unite}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Unavailable Dishes */}
        <Grid item xs={12} lg={6}>
          <Card sx={{ height: '100%', borderRadius: 3, border: '1px solid #f1f5f9' }}>
            <CardContent sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <RestaurantIcon sx={{ color: '#64748b' }} />
                  <Typography variant="h6" fontWeight={700}>
                    Plats Actuellement Indisponibles
                  </Typography>
                </Box>
                <Chip
                  label={`${unavailableDishes.length} plat(s)`}
                  size="small"
                  sx={{ fontWeight: 700 }}
                />
              </Box>

              {unavailableDishes.length === 0 ? (
                <Paper elevation={0} sx={{ p: 4, textAlign: 'center', bgcolor: '#f8fafc', borderRadius: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Tous les plats du menu sont actuellement disponibles à la commande.
                  </Typography>
                </Paper>
              ) : (
                <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid #f1f5f9', borderRadius: 2 }}>
                  <Table size="small">
                    <TableHead sx={{ bgcolor: '#f8fafc' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 700 }}>Plat</TableCell>
                        <TableCell sx={{ fontWeight: 700 }}>Catégorie</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 700 }}>Prix</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {unavailableDishes.map((dish) => (
                        <TableRow key={dish.id} hover>
                          <TableCell sx={{ fontWeight: 600 }}>{dish.nom}</TableCell>
                          <TableCell>{dish.categorie}</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 700 }}>
                            {dish.prix.toFixed(2)} €
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}


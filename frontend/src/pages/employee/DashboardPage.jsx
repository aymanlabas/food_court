import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Typography,
  Grid,
  Button,
  Paper,
  Card,
  CardContent,
} from '@mui/material';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import useAuthStore from '../../store/authStore';
import * as menuApi from '../../api/menuApi';
import * as orderApi from '../../api/orderApi';
import { formatPrice } from '../../utils/formatters';
import StatsCard from '../../components/common/StatsCard';
import DishCard from '../../components/menu/DishCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export default function DashboardPage() {
  const { user } = useAuthStore();

  const { data: dishes = [], isLoading } = useQuery({
    queryKey: ['availableDishes'],
    queryFn: menuApi.getAvailableDishes,
  });

  const { data: userOrders = [] } = useQuery({
    queryKey: ['userOrders', user?.id],
    queryFn: () => orderApi.getUserOrders(user?.id),
    enabled: Boolean(user?.id),
  });

  const activeOrders = userOrders.filter(
    (o) => o.statut !== 'SERVI' && o.statut !== 'ANNULE'
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Welcome Banner */}
      <Paper
        elevation={0}
        sx={{
          p: { xs: 3, md: 4 },
          borderRadius: 4,
          background: 'linear-gradient(135deg, #15803d 0%, #16a34a 100%)',
          color: '#ffffff',
          display: 'flex',
          flexDirection: { xs: 'column', sm: 'row' },
          justifyContent: 'space-between',
          alignItems: { xs: 'flex-start', sm: 'center' },
          gap: 2,
          boxShadow: '0 10px 25px -5px rgba(22, 163, 74, 0.25)',
        }}
      >
        <Box>
          <Typography variant="h4" fontWeight={800} sx={{ letterSpacing: '-0.02em', mb: 0.5 }}>
            Bonjour, {user?.nom || 'Collaborateur'} ! 👋
          </Typography>
          <Typography variant="body1" sx={{ opacity: 0.9 }}>
            Prêt pour votre pause déjeuner ? Découvrez les plats préparés aujourd'hui.
          </Typography>
        </Box>
        <Button
          component={RouterLink}
          to="/menu"
          variant="contained"
          endIcon={<ArrowForwardIcon />}
          sx={{
            bgcolor: '#ffffff',
            color: '#15803d',
            fontWeight: 700,
            px: 3,
            py: 1.2,
            borderRadius: 3,
            '&:hover': {
              bgcolor: '#f8fafc',
            },
          }}
        >
          Commander maintenant
        </Button>
      </Paper>

      {/* Stats row */}
      <Grid container spacing={3}>
        <Grid item xs={12} sm={4}>
          <StatsCard
            title="Solde badge cantine"
            value={formatPrice(user?.soldeCantine || 0)}
            subtitle="Débit direct et sécurisé à chaque commande"
            icon={AccountBalanceWalletIcon}
            color="#16a34a"
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatsCard
            title="Plats disponibles aujourd'hui"
            value={dishes.length}
            subtitle="Cuisinés frais sur place par nos chefs"
            icon={RestaurantMenuIcon}
            color="#3b82f6"
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatsCard
            title="Mes commandes actives"
            value={activeOrders.length}
            subtitle={activeOrders.length > 0 ? "En cours de préparation en cuisine" : "Aucune commande active en préparation"}
            icon={ReceiptLongIcon}
            color="#f59e0b"
          />
        </Grid>
      </Grid>

      {/* Today's Menu Highlight */}
      <Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5 }}>
          <Box>
            <Typography variant="h5" fontWeight={800} color="text.primary">
              Au menu aujourd'hui 🍽️
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sélection de plats du jour préparés avec des ingrédients frais.
            </Typography>
          </Box>
          <Button
            component={RouterLink}
            to="/menu"
            color="primary"
            endIcon={<ArrowForwardIcon />}
            sx={{ fontWeight: 700 }}
          >
            Tout voir
          </Button>
        </Box>

        {isLoading ? (
          <LoadingSpinner message="Chargement des plats du jour..." />
        ) : dishes.length === 0 ? (
          <Card sx={{ p: 4, textAlign: 'center', bgcolor: '#ffffff' }}>
            <Typography variant="body1" color="text.secondary">
              Aucun plat disponible pour le moment. Revenez plus tard !
            </Typography>
          </Card>
        ) : (
          <Grid container spacing={3}>
            {dishes.slice(0, 3).map((dish) => (
              <Grid item xs={12} sm={6} md={4} key={dish.id}>
                <DishCard dish={dish} />
              </Grid>
            ))}
          </Grid>
        )}
      </Box>

      {/* Recent Orders Section */}
      <Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" fontWeight={800} color="text.primary">
            Dernières commandes
          </Typography>
          {userOrders.length > 0 && (
            <Button component={RouterLink} to="/orders" color="primary" endIcon={<ArrowForwardIcon />}>
              Voir tout l'historique
            </Button>
          )}
        </Box>

        {userOrders.length === 0 ? (
          <Card sx={{ p: 4, textAlign: 'center', bgcolor: '#ffffff', borderRadius: 3 }}>
            <CardContent>
              <Typography variant="body1" color="text.secondary" fontWeight={500}>
                Vous n'avez pas encore passé de commande aujourd'hui.
              </Typography>
              <Button
                component={RouterLink}
                to="/menu"
                variant="outlined"
                color="primary"
                sx={{ mt: 2 }}
              >
                Consulter le menu et commander
              </Button>
            </CardContent>
          </Card>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {userOrders.slice(0, 2).map((order) => (
              <Card key={order.id} sx={{ p: 2.5, borderRadius: 3, border: '1px solid #f1f5f9' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
                  <Box>
                    <Typography variant="subtitle1" fontWeight={700}>
                      Commande #{order.id} — {formatPrice(order.montantTotal)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Statut : {order.statut}
                    </Typography>
                  </Box>
                  <Button
                    component={RouterLink}
                    to={`/orders/${order.id}`}
                    variant="outlined"
                    size="small"
                  >
                    Suivre la commande
                  </Button>
                </Box>
              </Card>
            ))}
          </Box>
        )}
      </Box>
    </Box>
  );
}


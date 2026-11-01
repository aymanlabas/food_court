import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Grid,
  Chip,
  Divider,
} from '@mui/material';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import useAuthStore from '../../store/authStore';
import * as orderApi from '../../api/orderApi';
import { formatPrice, formatDate } from '../../utils/formatters';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export default function OrderHistoryPage() {
  const { user } = useAuthStore();

  const { data: orders = [], isLoading } = useQuery({
    queryKey: ['userOrders', user?.id],
    queryFn: () => orderApi.getUserOrders(user?.id),
    enabled: Boolean(user?.id),
  });

  return (
    <Box sx={{ maxWidth: 900, mx: 'auto', py: 3, pb: 8 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Mes Commandes 📋
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Retrouvez l'historique complet de vos déjeuners et repas commandés.
          </Typography>
        </Box>
        <Button
          component={RouterLink}
          to="/menu"
          variant="contained"
          color="primary"
          endIcon={<ArrowForwardIcon />}
        >
          Nouvelle commande
        </Button>
      </Box>

      {isLoading ? (
        <LoadingSpinner message="Chargement de vos commandes..." />
      ) : orders.length === 0 ? (
        <Card sx={{ p: 6, textAlign: 'center', borderRadius: 4, border: '1px solid #f1f5f9' }}>
          <ReceiptLongIcon sx={{ fontSize: 64, color: '#94a3b8', mb: 2 }} />
          <Typography variant="h6" fontWeight={700} color="text.primary" sx={{ mb: 1 }}>
            Aucune commande enregistrée
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Vous n'avez pas encore passé de commande à la cantine.
          </Typography>
          <Button component={RouterLink} to="/menu" variant="contained" color="primary">
            Commander un plat
          </Button>
        </Card>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
          {orders.map((order) => (
            <Card
              key={order.id}
              sx={{
                borderRadius: 3.5,
                border: '1px solid #f1f5f9',
                boxShadow: '0 1px 3px 0 rgba(0,0,0,0.04)',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: '0 8px 20px -4px rgba(0,0,0,0.08)',
                },
              }}
            >
              <CardContent sx={{ p: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 2, mb: 2 }}>
                  <Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.5 }}>
                      <Typography variant="h6" fontWeight={800}>
                        Commande #{order.id}
                      </Typography>
                      <StatusBadge status={order.statut} />
                    </Box>
                    <Typography variant="caption" color="text.secondary">
                      {formatDate(order.date || order.createdAt)}
                    </Typography>
                  </Box>

                  <Typography variant="h6" fontWeight={800} color="#16a34a">
                    {formatPrice(order.montantTotal)}
                  </Typography>
                </Box>

                <Divider sx={{ my: 1.5 }} />

                {/* Items summary */}
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                  {order.items?.map((item) => (
                    <Chip
                      key={item.id}
                      icon={<RestaurantIcon sx={{ fontSize: '1rem !important' }} />}
                      label={`${item.dishNom || `Plat #${item.dishId}`} (×${item.quantite})`}
                      size="small"
                      sx={{ bgcolor: '#f8fafc', fontWeight: 600, color: '#334155' }}
                    />
                  ))}
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <Button
                    component={RouterLink}
                    to={`/orders/${order.id}`}
                    variant="outlined"
                    color="primary"
                    size="small"
                    endIcon={<ArrowForwardIcon />}
                  >
                    Suivre la préparation
                  </Button>
                </Box>
              </CardContent>
            </Card>
          ))}
        </Box>
      )}
    </Box>
  );
}

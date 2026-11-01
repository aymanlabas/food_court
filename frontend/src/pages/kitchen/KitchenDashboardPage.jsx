import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  IconButton,
  Paper,
  Divider,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import * as orderApi from '../../api/orderApi';
import { formatPrice, formatDate } from '../../utils/formatters';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const COLUMNS = [
  { key: 'RECU', label: 'Nouvelles Commandes', color: '#f59e0b', bg: '#fef3c7' },
  { key: 'EN_PREPARATION', label: 'En Préparation', color: '#3b82f6', bg: '#dbeafe' },
  { key: 'PRET', label: 'Prêt au Comptoir', color: '#16a34a', bg: '#dcfce7' },
];

export default function KitchenDashboardPage() {
  const queryClient = useQueryClient();

  const { data: orders = [], isLoading, refetch } = useQuery({
    queryKey: ['kitchenOrders'],
    queryFn: orderApi.getKitchenOrders,
    refetchInterval: 5000, // Refresh every 5s for real-time kitchen experience
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, statut }) => orderApi.updateOrderStatus(id, statut),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['kitchenOrders'] });
      queryClient.invalidateQueries({ queryKey: ['allOrders'] });
    },
  });

  const getOrdersByStatus = (status) => orders.filter((o) => o.statut === status);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, pb: 8 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Écran de Contrôle Cuisine 👨‍🍳
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Suivi des commandes en direct, gestion du flux de préparation et service au comptoir.
          </Typography>
        </Box>
        <Button
          variant="outlined"
          color="inherit"
          startIcon={<RefreshIcon />}
          onClick={() => refetch()}
          sx={{ fontWeight: 600 }}
        >
          Actualiser
        </Button>
      </Box>

      {/* Kanban Board */}
      {isLoading ? (
        <LoadingSpinner message="Chargement des commandes de cuisine..." />
      ) : (
        <Grid container spacing={3}>
          {COLUMNS.map((col) => {
            const colOrders = getOrdersByStatus(col.key);
            return (
              <Grid item xs={12} md={4} key={col.key}>
                <Paper
                  elevation={0}
                  sx={{
                    p: 2,
                    borderRadius: 3.5,
                    bgcolor: '#f8fafc',
                    border: '1px solid #e2e8f0',
                    minHeight: 600,
                  }}
                >
                  {/* Column Header */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, px: 1 }}>
                    <Typography variant="subtitle1" fontWeight={800} color="text.primary">
                      {col.label}
                    </Typography>
                    <Chip
                      label={colOrders.length}
                      size="small"
                      sx={{ bgcolor: col.bg, color: col.color, fontWeight: 700 }}
                    />
                  </Box>

                  {/* Orders list in column */}
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    {colOrders.length === 0 ? (
                      <Box sx={{ p: 4, textAlign: 'center', bgcolor: '#ffffff', borderRadius: 3, border: '1px dashed #cbd5e1' }}>
                        <Typography variant="caption" color="text.secondary">
                          Aucune commande à cette étape
                        </Typography>
                      </Box>
                    ) : (
                      colOrders.map((order) => (
                        <Card
                          key={order.id}
                          sx={{
                            borderRadius: 3,
                            border: '1px solid #e2e8f0',
                            boxShadow: '0 2px 8px -2px rgba(0,0,0,0.06)',
                          }}
                        >
                          <CardContent sx={{ p: 2.5, pb: 1.5 }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                              <Typography variant="h6" fontWeight={800}>
                                #{order.id}
                              </Typography>
                              <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                <AccessTimeIcon fontSize="inherit" />
                                {formatDate(order.date || order.createdAt)}
                              </Typography>
                            </Box>

                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                              <PersonOutlineIcon fontSize="small" sx={{ color: '#64748b' }} />
                              <Typography variant="body2" fontWeight={600} color="text.secondary">
                                {order.userName || `Client #${order.userId}`}
                              </Typography>
                            </Box>

                            <Divider sx={{ my: 1 }} />

                            {/* Dishes List */}
                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.8, my: 1.5 }}>
                              {order.items?.map((item) => (
                                <Box
                                  key={item.id}
                                  sx={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    bgcolor: '#f1f5f9',
                                    px: 1.5,
                                    py: 0.8,
                                    borderRadius: 2,
                                  }}
                                >
                                  <Typography variant="body2" fontWeight={700}>
                                    {item.dishNom || `Plat #${item.dishId}`}
                                  </Typography>
                                  <Chip
                                    label={`× ${item.quantite}`}
                                    size="small"
                                    color="primary"
                                    sx={{ fontWeight: 800, height: 22 }}
                                  />
                                </Box>
                              ))}
                            </Box>
                          </CardContent>

                          {/* Action Button */}
                          <CardActions sx={{ px: 2.5, pb: 2, pt: 0 }}>
                            {order.statut === 'RECU' && (
                              <Button
                                fullWidth
                                variant="contained"
                                color="warning"
                                startIcon={<PlayArrowIcon />}
                                disabled={statusMutation.isPending}
                                onClick={() => statusMutation.mutate({ id: order.id, statut: 'EN_PREPARATION' })}
                                sx={{ borderRadius: 2 }}
                              >
                                Démarrer la préparation
                              </Button>
                            )}

                            {order.statut === 'EN_PREPARATION' && (
                              <Button
                                fullWidth
                                variant="contained"
                                color="primary"
                                startIcon={<CheckCircleIcon />}
                                disabled={statusMutation.isPending}
                                onClick={() => statusMutation.mutate({ id: order.id, statut: 'PRET' })}
                                sx={{ borderRadius: 2 }}
                              >
                                Marquer prêt au comptoir
                              </Button>
                            )}

                            {order.statut === 'PRET' && (
                              <Button
                                fullWidth
                                variant="contained"
                                color="success"
                                startIcon={<DoneAllIcon />}
                                disabled={statusMutation.isPending}
                                onClick={() => statusMutation.mutate({ id: order.id, statut: 'SERVI' })}
                                sx={{ borderRadius: 2 }}
                              >
                                Remettre au client (Servi)
                              </Button>
                            )}
                          </CardActions>
                        </Card>
                      ))
                    )}
                  </Box>
                </Paper>
              </Grid>
            );
          })}
        </Grid>
      )}
    </Box>
  );
}

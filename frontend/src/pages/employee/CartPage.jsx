import React, { useState } from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Divider,
  IconButton,
  Alert,
  CircularProgress,
  Chip,
  Paper,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import useCartStore from '../../store/cartStore';
import useAuthStore from '../../store/authStore';
import * as orderApi from '../../api/orderApi';
import { formatPrice } from '../../utils/formatters';

export default function CartPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { items, updateQuantity, removeItem, clearCart, getTotal } = useCartStore();
  const { user, setUser } = useAuthStore();
  const [errorMsg, setErrorMsg] = useState(null);

  const estimatedTotal = getTotal ? getTotal() : items.reduce((sum, item) => sum + item.dish.prix * item.quantity, 0);
  const currentBalance = user?.soldeCantine || 0;
  const isBalanceSufficient = currentBalance >= estimatedTotal;

  const orderMutation = useMutation({
    mutationFn: orderApi.createOrder,
    onSuccess: (order) => {
      // Mettre à jour le solde utilisateur dans le store
      if (user) {
        setUser({
          ...user,
          soldeCantine: Math.max(0, currentBalance - order.montantTotal),
        });
      }
      clearCart();
      queryClient.invalidateQueries({ queryKey: ['userOrders'] });
      queryClient.invalidateQueries({ queryKey: ['availableDishes'] });
      navigate(`/orders/${order.id}`);
    },
    onError: (err) => {
      setErrorMsg(
        err.response?.data?.message ||
        "Une erreur est survenue lors de la création de la commande."
      );
    },
  });

  const handleCheckout = () => {
    setErrorMsg(null);

    if (!user) {
      navigate('/login');
      return;
    }

    if (!isBalanceSufficient) {
      setErrorMsg("Votre solde badge cantine est insuffisant pour régler cette commande.");
      return;
    }

    // Préparation de la commande
    const payload = {
      userId: user.id,
      items: items.map((item) => ({
        dishId: item.dish.id,
        quantite: item.quantity,
      })),
      idempotencyKey: crypto.randomUUID(),
    };

    orderMutation.mutate(payload);
  };

  return (
    <Box sx={{ maxWidth: 860, mx: 'auto', py: 3, pb: 8 }}>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
          Mon Panier Repas 🍽️
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Vérifiez vos choix et réglez directement avec votre badge cantine.
        </Typography>
      </Box>

      {errorMsg && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: 3 }}>
          {errorMsg}
        </Alert>
      )}

      {items.length === 0 ? (
        <Card sx={{ p: 6, textAlign: 'center', borderRadius: 4, border: '1px solid #f1f5f9' }}>
          <ShoppingCartIcon sx={{ fontSize: 64, color: '#94a3b8', mb: 2 }} />
          <Typography variant="h6" fontWeight={700} color="text.primary" sx={{ mb: 1 }}>
            Votre panier est vide
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Découvrez nos spécialités fraîches du jour et composez votre déjeuner.
          </Typography>
          <Button
            component={RouterLink}
            to="/menu"
            variant="contained"
            color="primary"
            startIcon={<ArrowForwardIcon />}
            sx={{ px: 3, py: 1.2 }}
          >
            Consulter le menu du jour
          </Button>
        </Card>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {/* Items list */}
          <Card sx={{ borderRadius: 4, border: '1px solid #f1f5f9' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
                Articles commandés ({items.reduce((s, i) => s + i.quantity, 0)})
              </Typography>

              {items.map((item) => (
                <Box
                  key={item.dish.id}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    py: 2,
                    borderBottom: '1px solid #f8fafc',
                    '&:last-child': { borderBottom: 0 },
                  }}
                >
                  <Box sx={{ flexGrow: 1, pr: 2 }}>
                    <Typography variant="body1" fontWeight={700}>
                      {item.dish.nom}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block">
                      Prix unitaire : {formatPrice(item.dish.prix)}
                    </Typography>
                  </Box>

                  {/* Quantity Stepper */}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mr: 3 }}>
                    <IconButton
                      size="small"
                      sx={{ border: '1px solid #e2e8f0', p: 0.5 }}
                      onClick={() => {
                        if (item.quantity > 1) {
                          updateQuantity(item.dish.id, item.quantity - 1);
                        } else {
                          removeItem(item.dish.id);
                        }
                      }}
                    >
                      <RemoveIcon fontSize="small" />
                    </IconButton>

                    <Typography variant="body2" fontWeight={700} sx={{ minWidth: 24, textAlign: 'center' }}>
                      {item.quantity}
                    </Typography>

                    <IconButton
                      size="small"
                      sx={{ border: '1px solid #e2e8f0', p: 0.5 }}
                      onClick={() => updateQuantity(item.dish.id, item.quantity + 1)}
                    >
                      <AddIcon fontSize="small" />
                    </IconButton>
                  </Box>

                  {/* Line Total */}
                  <Typography variant="body1" fontWeight={800} color="#16a34a" sx={{ minWidth: 80, textAlign: 'right' }}>
                    {formatPrice(item.dish.prix * item.quantity)}
                  </Typography>

                  <IconButton
                    size="small"
                    color="error"
                    sx={{ ml: 1 }}
                    onClick={() => removeItem(item.dish.id)}
                  >
                    <DeleteOutlineIcon fontSize="small" />
                  </IconButton>
                </Box>
              ))}
            </CardContent>
          </Card>

          {/* Badge Balance & Checkout Summary */}
          <Paper
            elevation={0}
            sx={{
              p: 3.5,
              borderRadius: 4,
              bgcolor: '#ffffff',
              border: '1px solid #f1f5f9',
              boxShadow: '0 4px 20px -2px rgba(0,0,0,0.05)',
            }}
          >
            {/* Badge balance status */}
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <AccountBalanceWalletIcon sx={{ color: isBalanceSufficient ? '#16a34a' : '#dc2626' }} />
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Votre solde badge cantine
                  </Typography>
                  <Typography variant="h6" fontWeight={800} color={isBalanceSufficient ? '#15803d' : '#dc2626'}>
                    {formatPrice(currentBalance)}
                  </Typography>
                </Box>
              </Box>

              <Chip
                label={isBalanceSufficient ? 'Solde suffisant' : 'Solde insuffisant'}
                color={isBalanceSufficient ? 'success' : 'error'}
                size="small"
                sx={{ fontWeight: 700 }}
              />
            </Box>

            <Divider sx={{ my: 2 }} />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
              <Typography variant="body2" color="text.secondary">
                Sous-total estimé (calcul autoritaire par le serveur) :
              </Typography>
              <Typography variant="body1" fontWeight={700}>
                {formatPrice(estimatedTotal)}
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6" fontWeight={800} color="text.primary">
                Total à régler :
              </Typography>
              <Typography variant="h4" fontWeight={800} color="#16a34a">
                {formatPrice(estimatedTotal)}
              </Typography>
            </Box>

            {!isBalanceSufficient && (
              <Alert severity="warning" sx={{ mb: 3, borderRadius: 2 }}>
                Votre solde cantine est insuffisant pour valider cette commande. Veuillez contacter l'administrateur pour recharger votre compte.
              </Alert>
            )}

            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', flexWrap: 'wrap' }}>
              <Button
                variant="outlined"
                color="inherit"
                onClick={clearCart}
                disabled={orderMutation.isPending}
              >
                Vider le panier
              </Button>

              <Button
                variant="contained"
                color="primary"
                size="large"
                disabled={!isBalanceSufficient || orderMutation.isPending}
                onClick={handleCheckout}
                startIcon={orderMutation.isPending ? <CircularProgress size={20} color="inherit" /> : <CheckCircleOutlineIcon />}
                sx={{ px: 4, py: 1.4, borderRadius: 3, fontWeight: 700 }}
              >
                {orderMutation.isPending ? 'Validation et débit en cours...' : 'Payer avec mon badge cantine'}
              </Button>
            </Box>
          </Paper>
        </Box>
      )}
    </Box>
  );
}

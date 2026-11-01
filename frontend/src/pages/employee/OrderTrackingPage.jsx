import React, { useState } from 'react';
import { useParams, Link as RouterLink } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Divider,
  Paper,
  Alert,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Chip,
  Grid,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import * as orderApi from '../../api/orderApi';
import { formatPrice, formatDate } from '../../utils/formatters';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import useAuthStore from '../../store/authStore';

const STEPS = [
  { label: 'Commande Reçue', description: 'Commande transmise au système' },
  { label: 'Ingrédients Réservés', description: 'Stock vérifié et alloué en cuisine' },
  { label: 'Badge Débité & En Préparation', description: 'Paiement confirmé, le chef prépare votre repas' },
  { label: 'Prêt au Comptoir', description: 'Votre plateau est prêt à être récupéré' },
  { label: 'Servi & Dégusté', description: 'Bon appétit !' },
];

export default function OrderTrackingPage() {
  const { id } = useParams();
  const queryClient = useQueryClient();
  const { user, setUser } = useAuthStore();
  const [isCancelDialogOpen, setIsCancelDialogOpen] = useState(false);

  const { data: order, isLoading, error } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderApi.getOrder(id),
    refetchInterval: 5000, // Sondage toutes les 5 secondes pour simuler le temps réel
  });

  const cancelMutation = useMutation({
    mutationFn: () => orderApi.cancelOrder(id),
    onSuccess: (updatedOrder) => {
      queryClient.invalidateQueries({ queryKey: ['order', id] });
      queryClient.invalidateQueries({ queryKey: ['userOrders'] });
      // Recréditer le solde cantine localement
      if (user && updatedOrder.montantTotal) {
        setUser({
          ...user,
          soldeCantine: (user.soldeCantine || 0) + updatedOrder.montantTotal,
        });
      }
      setIsCancelDialogOpen(false);
    },
  });

  if (isLoading) {
    return <LoadingSpinner message="Recherche de la commande..." />;
  }

  if (error || !order) {
    return (
      <Box sx={{ maxWidth: 600, mx: 'auto', py: 6, textAlign: 'center' }}>
        <Alert severity="error" sx={{ mb: 3 }}>
          Impossible de trouver cette commande ou vous n'avez pas les autorisations nécessaires.
        </Alert>
        <Button component={RouterLink} to="/dashboard" variant="contained">
          Retour au tableau de bord
        </Button>
      </Box>
    );
  }

  const getActiveStep = () => {
    switch (order.statut) {
      case 'RECU':
        return 1;
      case 'EN_PREPARATION':
        return 2;
      case 'PRET':
        return 3;
      case 'SERVI':
        return 5;
      case 'ANNULE':
        return -1;
      default:
        return 0;
    }
  };

  const isCancelled = order.statut === 'ANNULE';
  const isServed = order.statut === 'SERVI';
  const canCancel = !isCancelled && !isServed;

  return (
    <Box sx={{ maxWidth: 860, mx: 'auto', py: 3, pb: 8 }}>
      {/* Back button and Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <Button
          component={RouterLink}
          to="/orders"
          startIcon={<ArrowBackIcon />}
          color="inherit"
          sx={{ fontWeight: 600 }}
        >
          Mes commandes
        </Button>
      </Box>

      {/* Main card */}
      <Card sx={{ borderRadius: 4, border: '1px solid #f1f5f9', mb: 3 }}>
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 2, mb: 3 }}>
            <Box>
              <Typography variant="overline" color="text.secondary" fontWeight={700}>
                Commande #{order.id}
              </Typography>
              <Typography variant="h4" fontWeight={800} color="text.primary">
                Suivi de Préparation ⏱️
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                Passée le {formatDate(order.date || order.createdAt)}
              </Typography>
            </Box>

            <Box sx={{ textAlign: 'right' }}>
              <StatusBadge status={order.statut} size="medium" />
              <Typography variant="h5" fontWeight={800} color="#16a34a" sx={{ mt: 1 }}>
                {formatPrice(order.montantTotal)}
              </Typography>
            </Box>
          </Box>

          <Divider sx={{ my: 3 }} />

          {/* Cancelled Banner */}
          {isCancelled ? (
            <Alert
              severity="error"
              icon={<CancelIcon fontSize="inherit" />}
              sx={{ borderRadius: 3, mb: 4, py: 2 }}
            >
              <Typography variant="subtitle2" fontWeight={700}>
                Cette commande a été annulée.
              </Typography>
              <Typography variant="body2">
                Le montant de <strong>{formatPrice(order.montantTotal)}</strong> a été automatiquement et intégralement recrédité sur votre badge cantine.
              </Typography>
            </Alert>
          ) : isServed ? (
            <Alert
              severity="success"
              icon={<CheckCircleIcon fontSize="inherit" />}
              sx={{ borderRadius: 3, mb: 4, py: 2 }}
            >
              <Typography variant="subtitle2" fontWeight={700}>
                Commande servie au comptoir !
              </Typography>
              <Typography variant="body2">
                Votre repas a été remis. Bon appétit !
              </Typography>
            </Alert>
          ) : null}

          {/* Progress Timeline */}
          {!isCancelled && (
            <Box sx={{ my: 4 }}>
              <Stepper activeStep={getActiveStep()} orientation="vertical">
                {STEPS.map((step, index) => (
                  <Step key={step.label} completed={getActiveStep() > index}>
                    <StepLabel
                      StepIconProps={{
                        sx: {
                          '&.Mui-completed': { color: '#16a34a' },
                          '&.Mui-active': { color: '#16a34a' },
                        },
                      }}
                    >
                      <Typography variant="subtitle1" fontWeight={700}>
                        {step.label}
                      </Typography>
                    </StepLabel>
                    <StepContent>
                      <Typography variant="body2" color="text.secondary" sx={{ pb: 2 }}>
                        {step.description}
                      </Typography>
                    </StepContent>
                  </Step>
                ))}
              </Stepper>
            </Box>
          )}

          <Divider sx={{ my: 3 }} />

          {/* Ordered Dishes Summary */}
          <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
            Détail des plats commandés
          </Typography>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {order.items?.map((item) => (
              <Box
                key={item.id}
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  p: 2,
                  bgcolor: '#f8fafc',
                  borderRadius: 2.5,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <RestaurantIcon sx={{ color: '#16a34a' }} fontSize="small" />
                  <Typography variant="body2" fontWeight={700}>
                    {item.dishNom || `Plat #${item.dishId}`}
                  </Typography>
                  <Chip label={`× ${item.quantite}`} size="small" sx={{ fontWeight: 700 }} />
                </Box>
                <Typography variant="body2" fontWeight={700} color="#16a34a">
                  {formatPrice((item.prixUnitaire || 0) * item.quantite)}
                </Typography>
              </Box>
            ))}
          </Box>

          {/* Cancellation button if permitted */}
          {canCancel && (
            <Box sx={{ mt: 4, pt: 3, borderTop: '1px solid #f1f5f9', display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                variant="outlined"
                color="error"
                onClick={() => setIsCancelDialogOpen(true)}
              >
                Annuler la commande & être remboursé
              </Button>
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Cancellation Confirmation Dialog */}
      <ConfirmDialog
        open={isCancelDialogOpen}
        title="Annuler cette commande ?"
        message={`Souhaitez-vous annuler la commande #${order.id} ? Le montant de ${formatPrice(order.montantTotal)} sera automatiquement recrédité sur votre badge cantine.`}
        confirmLabel="Confirmer l'annulation"
        confirmColor="error"
        loading={cancelMutation.isPending}
        onClose={() => setIsCancelDialogOpen(false)}
        onConfirm={() => cancelMutation.mutate()}
      />
    </Box>
  );
}

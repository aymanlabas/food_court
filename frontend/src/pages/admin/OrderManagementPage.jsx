import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Chip,
  IconButton,
  Button,
  Grid,
} from '@mui/material';
import CancelIcon from '@mui/icons-material/Cancel';
import RefreshIcon from '@mui/icons-material/Refresh';
import * as orderApi from '../../api/orderApi';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import StatsCard from '../../components/common/StatsCard';
import { formatPrice, formatDate } from '../../utils/formatters';

const STATUS_FILTERS = ['ALL', 'RECU', 'EN_PREPARATION', 'PRET', 'SERVI', 'ANNULE'];

export default function OrderManagementPage() {
  const queryClient = useQueryClient();
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [cancellingOrder, setCancellingOrder] = useState(null);

  const { data: orders = [], isLoading, refetch } = useQuery({
    queryKey: ['allOrders'],
    queryFn: orderApi.getOrders,
  });

  const { data: stats } = useQuery({
    queryKey: ['orderStats'],
    queryFn: orderApi.getOrderStats,
  });

  const cancelMutation = useMutation({
    mutationFn: (id) => orderApi.cancelOrder(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['allOrders'] });
      queryClient.invalidateQueries({ queryKey: ['orderStats'] });
      setCancellingOrder(null);
    },
  });

  const filteredOrders = orders.filter((o) =>
    selectedStatus === 'ALL' ? true : o.statut === selectedStatus
  );

  const columns = [
    {
      id: 'id',
      label: '#ID',
      render: (row) => (
        <Typography variant="body2" fontWeight={700}>
          #{row.id}
        </Typography>
      ),
    },
    {
      id: 'date',
      label: 'Date & Heure',
      render: (row) => formatDate(row.date || row.createdAt),
    },
    {
      id: 'user',
      label: 'Collaborateur',
      render: (row) => (
        <Box>
          <Typography variant="body2" fontWeight={700}>
            {row.userName || `User #${row.userId}`}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {row.userEmail || ''}
          </Typography>
        </Box>
      ),
    },
    {
      id: 'items',
      label: 'Articles',
      render: (row) => (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
          {row.items?.map((item) => (
            <Chip
              key={item.id}
              label={`${item.dishNom || item.dishId} (×${item.quantite})`}
              size="small"
              sx={{ fontSize: '0.72rem' }}
            />
          ))}
        </Box>
      ),
    },
    {
      id: 'montantTotal',
      label: 'Montant Total',
      align: 'right',
      render: (row) => (
        <Typography variant="body2" fontWeight={800} color="#16a34a">
          {formatPrice(row.montantTotal)}
        </Typography>
      ),
    },
    {
      id: 'statut',
      label: 'Statut',
      align: 'center',
      render: (row) => <StatusBadge status={row.statut} />,
    },
    {
      id: 'actions',
      label: 'Actions',
      align: 'right',
      render: (row) => (
        row.statut !== 'SERVI' && row.statut !== 'ANNULE' ? (
          <Button
            size="small"
            color="error"
            variant="outlined"
            onClick={() => setCancellingOrder(row)}
          >
            Annuler & Rembourser
          </Button>
        ) : (
          <Typography variant="caption" color="text.secondary">—</Typography>
        )
      ),
    },
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5, pb: 8 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Supervision Globale des Commandes 📊
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Contrôle en temps réel des commandes, états de préparation et chiffre d'affaires cantine.
          </Typography>
        </Box>
        <Button variant="outlined" color="inherit" startIcon={<RefreshIcon />} onClick={() => refetch()}>
          Actualiser
        </Button>
      </Box>

      {/* Stats row */}
      {stats && (
        <Grid container spacing={2.5}>
          <Grid item xs={12} sm={6} md={2.4}>
            <StatsCard title="Total Commandes" value={stats.totalOrders} color="#3b82f6" />
          </Grid>
          <Grid item xs={12} sm={6} md={2.4}>
            <StatsCard title="En Préparation" value={stats.enPreparationOrders} color="#f59e0b" />
          </Grid>
          <Grid item xs={12} sm={6} md={2.4}>
            <StatsCard title="Prêtes à Servir" value={stats.pretOrders} color="#16a34a" />
          </Grid>
          <Grid item xs={12} sm={6} md={2.4}>
            <StatsCard title="Commandes Servies" value={stats.serviOrders} color="#059669" />
          </Grid>
          <Grid item xs={12} sm={6} md={2.4}>
            <StatsCard title="Chiffre d'Affaires" value={formatPrice(stats.totalRevenue)} color="#8b5cf6" />
          </Grid>
        </Grid>
      )}

      {/* Filter Chips */}
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        {STATUS_FILTERS.map((s) => (
          <Chip
            key={s}
            label={s === 'ALL' ? 'Toutes les commandes' : s}
            clickable
            onClick={() => setSelectedStatus(s)}
            sx={{
              fontWeight: selectedStatus === s ? 700 : 500,
              bgcolor: selectedStatus === s ? '#16a34a' : '#ffffff',
              color: selectedStatus === s ? '#ffffff' : '#475569',
              border: '1px solid',
              borderColor: selectedStatus === s ? '#16a34a' : '#cbd5e1',
            }}
          />
        ))}
      </Box>

      {/* Data Table */}
      <DataTable
        columns={columns}
        rows={filteredOrders}
        loading={isLoading}
        emptyMessage="Aucune commande trouvée pour ce statut."
      />

      {/* Cancel Confirmation */}
      <ConfirmDialog
        open={Boolean(cancellingOrder)}
        title="Annuler cette commande administrativement ?"
        message={`Confirmez-vous l'annulation de la commande #${cancellingOrder?.id} ? Le badge du collaborateur sera automatiquement recrédité de ${formatPrice(cancellingOrder?.montantTotal || 0)}.`}
        confirmLabel="Confirmer l'annulation"
        confirmColor="error"
        loading={cancelMutation.isPending}
        onClose={() => setCancellingOrder(null)}
        onConfirm={() => cancelMutation.mutate(cancellingOrder.id)}
      />
    </Box>
  );
}

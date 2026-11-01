import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Chip,
  Paper,
  Button,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import PaymentIcon from '@mui/icons-material/Payment';
import api from '../../api/axiosInstance';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import { formatPrice, formatDate } from '../../utils/formatters';

export default function PaymentManagementPage() {
  const { data: pageData, isLoading, refetch } = useQuery({
    queryKey: ['adminPayments'],
    queryFn: () => api.get('/payments?size=50').then((r) => r.data),
  });

  const payments = pageData?.content || [];

  const columns = [
    {
      id: 'id',
      label: '#Paiement',
      render: (row) => (
        <Typography variant="body2" fontWeight={700}>
          #{row.id}
        </Typography>
      ),
    },
    {
      id: 'mealOrderId',
      label: '#Commande',
      render: (row) => (
        <Typography variant="body2" fontWeight={600} color="#3b82f6">
          #{row.mealOrderId}
        </Typography>
      ),
    },
    {
      id: 'userId',
      label: '#Collaborateur',
      render: (row) => `User #${row.userId}`,
    },
    {
      id: 'montant',
      label: 'Montant Débité',
      align: 'right',
      render: (row) => (
        <Typography variant="body2" fontWeight={700} color="#16a34a">
          {formatPrice(row.montant)}
        </Typography>
      ),
    },
    {
      id: 'moyen',
      label: 'Moyen de Paiement',
      render: (row) => (
        <Chip label={row.moyen || 'BADGE'} size="small" variant="outlined" sx={{ fontWeight: 600 }} />
      ),
    },
    {
      id: 'statut',
      label: 'Statut Transaction',
      align: 'center',
      render: (row) => <StatusBadge status={row.statut} />,
    },
    {
      id: 'idempotencyKey',
      label: 'Clé d’Idempotence (Anti-Doublon)',
      render: (row) => (
        <Typography variant="caption" sx={{ fontFamily: 'monospace', color: '#64748b' }}>
          {row.idempotencyKey ? `${row.idempotencyKey.substring(0, 16)}…` : '—'}
        </Typography>
      ),
    },
    {
      id: 'createdAt',
      label: 'Date & Heure',
      render: (row) => formatDate(row.createdAt),
    },
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5, pb: 8 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Journal des Paiements & Transactions 💳
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Traçabilité financière intégrale des débits badges cantine et remboursements idempotents.
          </Typography>
        </Box>
        <Button variant="outlined" color="inherit" startIcon={<RefreshIcon />} onClick={() => refetch()}>
          Actualiser
        </Button>
      </Box>

      {/* Data Table */}
      <DataTable
        columns={columns}
        rows={payments}
        loading={isLoading}
        emptyMessage="Aucune transaction de paiement enregistrée pour le moment."
      />
    </Box>
  );
}

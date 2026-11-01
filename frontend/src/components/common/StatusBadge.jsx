import React from 'react';
import { Chip } from '@mui/material';

export default function StatusBadge({ status, available, label, size = 'small' }) {
  if (available !== undefined) {
    return (
      <Chip
        label={available ? 'Disponible' : 'Indisponible'}
        color={available ? 'success' : 'default'}
        size={size}
        sx={{
          fontWeight: 600,
          backgroundColor: available ? '#dcfce7' : '#f1f5f9',
          color: available ? '#15803d' : '#64748b',
          border: '1px solid',
          borderColor: available ? '#bbf7d0' : '#e2e8f0',
        }}
      />
    );
  }

  const getStatusProps = () => {
    switch (status) {
      case 'CONFIRME':
      case 'PRET':
      case 'SERVI':
        return { bg: '#dcfce7', text: '#15803d', border: '#bbf7d0', defaultLabel: 'Confirmé' };
      case 'EN_PREPARATION':
      case 'EN_ATTENTE':
        return { bg: '#fef3c7', text: '#b45309', border: '#fde68a', defaultLabel: 'En cours' };
      case 'ANNULE':
      case 'ECHOUE':
        return { bg: '#fee2e2', text: '#b91c1c', border: '#fecaca', defaultLabel: 'Annulé' };
      case 'REMBOURSE':
        return { bg: '#e0e7ff', text: '#4338ca', border: '#c7d2fe', defaultLabel: 'Remboursé' };
      default:
        return { bg: '#f1f5f9', text: '#475569', border: '#cbd5e1', defaultLabel: status };
    }
  };

  const style = getStatusProps();

  return (
    <Chip
      label={label || style.defaultLabel}
      size={size}
      sx={{
        fontWeight: 600,
        backgroundColor: style.bg,
        color: style.text,
        border: '1px solid',
        borderColor: style.border,
      }}
    />
  );
}


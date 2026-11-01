import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Button,
  IconButton,
  Chip,
  Snackbar,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import * as userApi from '../../api/userApi';
import DataTable from '../../components/common/DataTable';
import EmployeeForm from '../../components/admin/EmployeeForm';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import SearchBar from '../../components/menu/SearchBar';
import { formatPrice } from '../../utils/formatters';

export default function EmployeeManagementPage() {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [deletingEmployee, setDeletingEmployee] = useState(null);
  const [creditingEmployee, setCreditingEmployee] = useState(null);
  const [creditAmount, setCreditAmount] = useState('20.00');
  const [toast, setToast] = useState(null);

  const { data: users = [], isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getUsers,
  });

  const registerMutation = useMutation({
    mutationFn: userApi.register,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsFormOpen(false);
      setToast({ type: 'success', message: 'Employé créé avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la création' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => userApi.updateUser(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsFormOpen(false);
      setEditingEmployee(null);
      setToast({ type: 'success', message: 'Profil employé mis à jour !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la mise à jour' });
    },
  });

  const creditMutation = useMutation({
    mutationFn: ({ id, montant }) => userApi.updateBalance(id, { montant }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setCreditingEmployee(null);
      setToast({ type: 'success', message: 'Solde cantine crédité avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors du crédit' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: userApi.deleteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setDeletingEmployee(null);
      setToast({ type: 'success', message: 'Employé supprimé avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la suppression' });
    },
  });

  const filteredUsers = users.filter((u) =>
    u.nom.toLowerCase().includes(searchQuery.toLowerCase()) ||
    u.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const columns = [
    {
      id: 'nom',
      label: 'Collaborateur',
      render: (row) => (
        <Box>
          <Typography variant="body2" fontWeight={700}>
            {row.nom}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {row.email}
          </Typography>
        </Box>
      ),
    },
    {
      id: 'role',
      label: 'Rôle',
      render: (row) => {
        const color = row.role === 'ADMIN' ? 'error' : row.role === 'KITCHEN' ? 'warning' : 'primary';
        return <Chip label={row.role} size="small" color={color} sx={{ fontWeight: 600 }} />;
      },
    },
    {
      id: 'soldeCantine',
      label: 'Solde Badge Cantine',
      align: 'right',
      render: (row) => (
        <Typography variant="body2" fontWeight={700} color="#16a34a">
          {formatPrice(row.soldeCantine)}
        </Typography>
      ),
    },
    {
      id: 'regimeAlimentaire',
      label: 'Régime Alimentaire',
      render: (row) => row.regimeAlimentaire || 'Non spécifié',
    },
    {
      id: 'actions',
      label: 'Actions',
      align: 'right',
      render: (row) => (
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
          <IconButton
            size="small"
            color="success"
            title="Créditer le badge"
            onClick={() => {
              setCreditingEmployee(row);
              setCreditAmount('20.00');
            }}
          >
            <AccountBalanceWalletIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color="primary"
            onClick={() => {
              setEditingEmployee(row);
              setIsFormOpen(true);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color="error"
            onClick={() => setDeletingEmployee(row)}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  const handleFormSubmit = (data) => {
    if (editingEmployee) {
      updateMutation.mutate({
        id: editingEmployee.id,
        data: {
          nom: data.nom,
          email: data.email,
          regimeAlimentaire: data.regimeAlimentaire,
        },
      });
    } else {
      registerMutation.mutate(data);
    }
  };

  const handleCreditSubmit = (e) => {
    e.preventDefault();
    if (!creditAmount || isNaN(creditAmount) || Number(creditAmount) <= 0) return;
    creditMutation.mutate({
      id: creditingEmployee.id,
      montant: parseFloat(creditAmount),
    });
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      {/* Top action bar */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Gestion des Employés & Soldes 👥
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Consultez les collaborateurs, rechargez leurs badges cantine et gérez leurs préférences alimentaires.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => {
            setEditingEmployee(null);
            setIsFormOpen(true);
          }}
          sx={{ py: 1.2, px: 2.5 }}
        >
          Ajouter un collaborateur
        </Button>
      </Box>

      {/* Search */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <SearchBar
          value={searchQuery}
          onChange={setSearchQuery}
          placeholder="Rechercher par nom ou email..."
        />
      </Box>

      {/* Data Table */}
      <DataTable
        columns={columns}
        rows={filteredUsers}
        loading={isLoading}
        emptyMessage="Aucun collaborateur trouvé."
      />

      {/* Create / Edit Form */}
      <EmployeeForm
        open={isFormOpen}
        employee={editingEmployee}
        loading={registerMutation.isPending || updateMutation.isPending}
        onClose={() => {
          setIsFormOpen(false);
          setEditingEmployee(null);
        }}
        onSubmit={handleFormSubmit}
      />

      {/* Credit Balance Dialog */}
      <Dialog open={Boolean(creditingEmployee)} onClose={() => setCreditingEmployee(null)} maxWidth="xs" fullWidth>
        <form onSubmit={handleCreditSubmit}>
          <DialogTitle fontWeight={700}>
            Créditer le badge de {creditingEmployee?.nom}
          </DialogTitle>
          <DialogContent dividers sx={{ pt: 2 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Solde actuel : <strong>{formatPrice(creditingEmployee?.soldeCantine || 0)}</strong>
            </Typography>
            <TextField
              label="Montant à créditer (€)"
              type="number"
              inputProps={{ step: '5.00', min: '1.00' }}
              value={creditAmount}
              onChange={(e) => setCreditAmount(e.target.value)}
              fullWidth
              required
              autoFocus
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, py: 2 }}>
            <Button onClick={() => setCreditingEmployee(null)} color="inherit" disabled={creditMutation.isPending}>
              Annuler
            </Button>
            <Button type="submit" variant="contained" color="success" disabled={creditMutation.isPending}>
              {creditMutation.isPending ? 'Crédit en cours...' : 'Créditer le compte'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={Boolean(deletingEmployee)}
        title="Supprimer ce compte collaborateur ?"
        message={`Confirmez-vous la suppression du compte de "${deletingEmployee?.nom}" (${deletingEmployee?.email}) ?`}
        confirmLabel="Supprimer"
        loading={deleteMutation.isPending}
        onClose={() => setDeletingEmployee(null)}
        onConfirm={() => deleteMutation.mutate(deletingEmployee.id)}
      />

      {/* Toast Alert */}
      <Snackbar
        open={Boolean(toast)}
        autoHideDuration={3000}
        onClose={() => setToast(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={toast?.type || 'info'} onClose={() => setToast(null)} sx={{ width: '100%', borderRadius: 2 }}>
          {toast?.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}


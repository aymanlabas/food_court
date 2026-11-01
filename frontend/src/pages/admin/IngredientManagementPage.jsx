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
import TuneIcon from '@mui/icons-material/Tune';
import * as menuApi from '../../api/menuApi';
import DataTable from '../../components/common/DataTable';
import IngredientForm from '../../components/admin/IngredientForm';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import SearchBar from '../../components/menu/SearchBar';

export default function IngredientManagementPage() {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingIngredient, setEditingIngredient] = useState(null);
  const [deletingIngredient, setDeletingIngredient] = useState(null);
  const [adjustingStock, setAdjustingStock] = useState(null);
  const [newStockValue, setNewStockValue] = useState('');
  const [toast, setToast] = useState(null);

  const { data: ingredients = [], isLoading } = useQuery({
    queryKey: ['ingredients'],
    queryFn: menuApi.getIngredients,
  });

  const createMutation = useMutation({
    mutationFn: menuApi.createIngredient,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] });
      queryClient.invalidateQueries({ queryKey: ['lowStockIngredients'] });
      setIsFormOpen(false);
      setToast({ type: 'success', message: 'Ingrédient ajouté avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la création' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => menuApi.updateIngredient(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] });
      queryClient.invalidateQueries({ queryKey: ['lowStockIngredients'] });
      setIsFormOpen(false);
      setEditingIngredient(null);
      setToast({ type: 'success', message: 'Ingrédient mis à jour !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la mise à jour' });
    },
  });

  const stockMutation = useMutation({
    mutationFn: ({ id, quantite }) => menuApi.updateStock(id, quantite),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] });
      queryClient.invalidateQueries({ queryKey: ['lowStockIngredients'] });
      queryClient.invalidateQueries({ queryKey: ['dishes'] });
      setAdjustingStock(null);
      setToast({ type: 'success', message: 'Stock mis à jour avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la mise à jour du stock' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: menuApi.deleteIngredient,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] });
      queryClient.invalidateQueries({ queryKey: ['lowStockIngredients'] });
      setDeletingIngredient(null);
      setToast({ type: 'success', message: 'Ingrédient supprimé avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Impossible de supprimer un ingrédient utilisé dans une recette' });
    },
  });

  const filtered = ingredients.filter((ing) =>
    ing.nom.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const columns = [
    {
      id: 'nom',
      label: 'Ingrédient',
      render: (row) => (
        <Typography variant="body2" fontWeight={700}>
          {row.nom}
        </Typography>
      ),
    },
    {
      id: 'unite',
      label: 'Unité',
      render: (row) => row.unite,
    },
    {
      id: 'stock',
      label: 'Quantité en Stock',
      align: 'right',
      render: (row) => {
        const isLow = Number(row.stock) <= Number(row.seuilAlerte);
        return (
          <Typography
            variant="body2"
            fontWeight={700}
            color={isLow ? '#dc2626' : '#0f172a'}
          >
            {row.stock} {row.unite}
          </Typography>
        );
      },
    },
    {
      id: 'seuilAlerte',
      label: 'Seuil d’Alerte',
      align: 'right',
      render: (row) => (
        <Typography variant="body2" color="text.secondary">
          {row.seuilAlerte} {row.unite}
        </Typography>
      ),
    },
    {
      id: 'status',
      label: 'État du stock',
      align: 'center',
      render: (row) => {
        const isLow = Number(row.stock) <= Number(row.seuilAlerte);
        return (
          <Chip
            size="small"
            label={isLow ? 'Stock Critique' : 'En Réserve'}
            color={isLow ? 'error' : 'success'}
            sx={{ fontWeight: 600 }}
          />
        );
      },
    },
    {
      id: 'actions',
      label: 'Actions',
      align: 'right',
      render: (row) => (
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
          <IconButton
            size="small"
            title="Ajuster le stock"
            onClick={() => {
              setAdjustingStock(row);
              setNewStockValue(row.stock);
            }}
          >
            <TuneIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color="primary"
            onClick={() => {
              setEditingIngredient(row);
              setIsFormOpen(true);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color="error"
            onClick={() => setDeletingIngredient(row)}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  const handleFormSubmit = (data) => {
    if (editingIngredient) {
      updateMutation.mutate({ id: editingIngredient.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const handleStockSubmit = (e) => {
    e.preventDefault();
    if (newStockValue === '' || isNaN(newStockValue)) return;
    stockMutation.mutate({
      id: adjustingStock.id,
      quantite: parseFloat(newStockValue),
    });
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      {/* Top action bar */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Inventaire des Ingrédients 📦
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gérez les stocks des matières premières utilisées pour la confection des repas.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => {
            setEditingIngredient(null);
            setIsFormOpen(true);
          }}
          sx={{ py: 1.2, px: 2.5 }}
        >
          Ajouter un ingrédient
        </Button>
      </Box>

      {/* Search Bar */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <SearchBar
          value={searchQuery}
          onChange={setSearchQuery}
          placeholder="Rechercher un ingrédient..."
        />
      </Box>

      {/* Data Table */}
      <DataTable
        columns={columns}
        rows={filtered}
        loading={isLoading}
        emptyMessage="Aucun ingrédient répertorié."
      />

      {/* Create / Edit Form */}
      <IngredientForm
        open={isFormOpen}
        ingredient={editingIngredient}
        loading={createMutation.isPending || updateMutation.isPending}
        onClose={() => {
          setIsFormOpen(false);
          setEditingIngredient(null);
        }}
        onSubmit={handleFormSubmit}
      />

      {/* Adjust Stock Dialog */}
      <Dialog open={Boolean(adjustingStock)} onClose={() => setAdjustingStock(null)} maxWidth="xs" fullWidth>
        <form onSubmit={handleStockSubmit}>
          <DialogTitle fontWeight={700}>
            Ajuster le stock : {adjustingStock?.nom}
          </DialogTitle>
          <DialogContent dividers sx={{ pt: 2 }}>
            <TextField
              label={`Nouvelle quantité en stock (${adjustingStock?.unite})`}
              type="number"
              inputProps={{ step: '0.001', min: '0' }}
              value={newStockValue}
              onChange={(e) => setNewStockValue(e.target.value)}
              fullWidth
              required
              autoFocus
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, py: 2 }}>
            <Button onClick={() => setAdjustingStock(null)} color="inherit" disabled={stockMutation.isPending}>
              Annuler
            </Button>
            <Button type="submit" variant="contained" color="primary" disabled={stockMutation.isPending}>
              {stockMutation.isPending ? 'Enregistrement...' : 'Mettre à jour'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        open={Boolean(deletingIngredient)}
        title="Supprimer cet ingrédient ?"
        message={`Voulez-vous supprimer "${deletingIngredient?.nom}" ? Cela ne fonctionnera que si cet ingrédient n'est rattaché à aucune recette active.`}
        confirmLabel="Supprimer"
        loading={deleteMutation.isPending}
        onClose={() => setDeletingIngredient(null)}
        onConfirm={() => deleteMutation.mutate(deletingIngredient.id)}
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


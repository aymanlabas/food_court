import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Button,
  IconButton,
  Switch,
  Snackbar,
  Alert,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import * as menuApi from '../../api/menuApi';
import DataTable from '../../components/common/DataTable';
import DishForm from '../../components/admin/DishForm';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/menu/SearchBar';
import CategoryFilter from '../../components/menu/CategoryFilter';
import { formatPrice, formatCategory } from '../../utils/formatters';

export default function DishManagementPage() {
  const queryClient = useQueryClient();
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingDish, setEditingDish] = useState(null);
  const [deletingDish, setDeletingDish] = useState(null);
  const [toast, setToast] = useState(null);

  const { data: dishes = [], isLoading } = useQuery({
    queryKey: ['dishes'],
    queryFn: () => menuApi.getDishes(),
  });

  const createMutation = useMutation({
    mutationFn: menuApi.createDish,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dishes'] });
      setIsFormOpen(false);
      setToast({ type: 'success', message: 'Plat créé avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la création' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => menuApi.updateDish(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dishes'] });
      setIsFormOpen(false);
      setEditingDish(null);
      setToast({ type: 'success', message: 'Plat mis à jour avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la mise à jour' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: menuApi.deleteDish,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dishes'] });
      setDeletingDish(null);
      setToast({ type: 'success', message: 'Plat supprimé avec succès !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur lors de la suppression' });
    },
  });

  const toggleMutation = useMutation({
    mutationFn: menuApi.toggleAvailability,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dishes'] });
    },
  });

  const filteredDishes = dishes.filter((dish) => {
    const matchCategory = selectedCategory === 'ALL' || dish.categorie === selectedCategory;
    const matchSearch =
      !searchQuery ||
      dish.nom.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (dish.description && dish.description.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchCategory && matchSearch;
  });

  const columns = [
    {
      id: 'nom',
      label: 'Plat',
      render: (row) => (
        <Box>
          <Typography variant="body2" fontWeight={700}>
            {row.nom}
          </Typography>
          <Typography variant="caption" color="text.secondary" noWrap sx={{ maxWidth: 260, display: 'block' }}>
            {row.description || '—'}
          </Typography>
        </Box>
      ),
    },
    {
      id: 'categorie',
      label: 'Catégorie',
      render: (row) => formatCategory(row.categorie),
    },
    {
      id: 'prix',
      label: 'Prix',
      align: 'right',
      render: (row) => (
        <Typography variant="body2" fontWeight={700} color="#16a34a">
          {formatPrice(row.prix)}
        </Typography>
      ),
    },
    {
      id: 'allergenes',
      label: 'Allergènes',
      render: (row) => row.allergenes || 'Aucun',
    },
    {
      id: 'disponible',
      label: 'Disponibilité',
      align: 'center',
      render: (row) => (
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1 }}>
          <StatusBadge available={row.disponible} />
          <Switch
            size="small"
            checked={row.disponible}
            onChange={() => toggleMutation.mutate(row.id)}
            color="primary"
          />
        </Box>
      ),
    },
    {
      id: 'actions',
      label: 'Actions',
      align: 'right',
      render: (row) => (
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
          <IconButton
            size="small"
            color="primary"
            onClick={() => {
              setEditingDish(row);
              setIsFormOpen(true);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color="error"
            onClick={() => setDeletingDish(row)}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  const handleFormSubmit = (data) => {
    if (editingDish) {
      updateMutation.mutate({ id: editingDish.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      {/* Top action bar */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Gestion des Plats 🍽️
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Ajoutez, modifiez ou activez les plats du menu de la cantine.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => {
            setEditingDish(null);
            setIsFormOpen(true);
          }}
          sx={{ py: 1.2, px: 2.5 }}
        >
          Ajouter un plat
        </Button>
      </Box>

      {/* Filter and search bar */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, justifyContent: 'space-between', gap: 2 }}>
        <CategoryFilter
          selectedCategory={selectedCategory}
          onSelectCategory={setSelectedCategory}
        />
        <SearchBar value={searchQuery} onChange={setSearchQuery} />
      </Box>

      {/* Data Table */}
      <DataTable
        columns={columns}
        rows={filteredDishes}
        loading={isLoading}
        emptyMessage="Aucun plat trouvé dans cette catégorie."
      />

      {/* Form Dialog */}
      <DishForm
        open={isFormOpen}
        dish={editingDish}
        loading={createMutation.isPending || updateMutation.isPending}
        onClose={() => {
          setIsFormOpen(false);
          setEditingDish(null);
        }}
        onSubmit={handleFormSubmit}
      />

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        open={Boolean(deletingDish)}
        title="Supprimer ce plat ?"
        message={`Voulez-vous vraiment supprimer définitivement "${deletingDish?.nom}" ? Cette action supprimera également ses recettes associées.`}
        confirmLabel="Supprimer"
        loading={deleteMutation.isPending}
        onClose={() => setDeletingDish(null)}
        onConfirm={() => deleteMutation.mutate(deletingDish.id)}
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


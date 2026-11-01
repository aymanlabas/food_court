import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Typography,
  Button,
  IconButton,
  TextField,
  MenuItem,
  Card,
  CardContent,
  Alert,
  Snackbar,
  Paper,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import * as menuApi from '../../api/menuApi';
import DataTable from '../../components/common/DataTable';
import RecipeLineForm from '../../components/admin/RecipeLineForm';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export default function RecipeManagementPage() {
  const queryClient = useQueryClient();
  const [selectedDishId, setSelectedDishId] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingRecipeLine, setEditingRecipeLine] = useState(null);
  const [deletingRecipeLine, setDeletingRecipeLine] = useState(null);
  const [toast, setToast] = useState(null);

  const { data: dishes = [], isLoading: loadingDishes } = useQuery({
    queryKey: ['dishes'],
    queryFn: () => menuApi.getDishes(),
  });

  const { data: ingredients = [] } = useQuery({
    queryKey: ['ingredients'],
    queryFn: menuApi.getIngredients,
  });

  // Default select first dish when dishes are loaded
  React.useEffect(() => {
    if (dishes.length > 0 && !selectedDishId) {
      setSelectedDishId(dishes[0].id);
    }
  }, [dishes, selectedDishId]);

  const { data: recipeLines = [], isLoading: loadingRecipes } = useQuery({
    queryKey: ['recipeLines', selectedDishId],
    queryFn: () => menuApi.getRecipeLines(selectedDishId),
    enabled: Boolean(selectedDishId),
  });

  const createMutation = useMutation({
    mutationFn: menuApi.createRecipeLine,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipeLines', selectedDishId] });
      queryClient.invalidateQueries({ queryKey: ['dishes'] });
      setIsFormOpen(false);
      setToast({ type: 'success', message: 'Ingrédient ajouté à la recette !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => menuApi.updateRecipeLine(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipeLines', selectedDishId] });
      setIsFormOpen(false);
      setEditingRecipeLine(null);
      setToast({ type: 'success', message: 'Recette mise à jour !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: menuApi.deleteRecipeLine,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipeLines', selectedDishId] });
      queryClient.invalidateQueries({ queryKey: ['dishes'] });
      setDeletingRecipeLine(null);
      setToast({ type: 'success', message: 'Ligne de recette retirée !' });
    },
    onError: (err) => {
      setToast({ type: 'error', message: err.response?.data?.message || 'Erreur' });
    },
  });

  const currentDish = dishes.find((d) => d.id === Number(selectedDishId));

  const columns = [
    {
      id: 'ingredientNom',
      label: 'Ingrédient',
      render: (row) => (
        <Typography variant="body2" fontWeight={700}>
          {row.ingredientNom}
        </Typography>
      ),
    },
    {
      id: 'quantite',
      label: 'Quantité Requise / Portion',
      align: 'right',
      render: (row) => {
        const ing = ingredients.find((i) => i.id === row.ingredientId);
        return (
          <Typography variant="body2" fontWeight={600} color="#16a34a">
            {row.quantite} {ing ? ing.unite : ''}
          </Typography>
        );
      },
    },
    {
      id: 'stockActuel',
      label: 'Stock Disponible en Cuisine',
      align: 'right',
      render: (row) => {
        const ing = ingredients.find((i) => i.id === row.ingredientId);
        if (!ing) return '—';
        const canMakePortions = Math.floor(Number(ing.stock) / Number(row.quantite));
        return (
          <Box sx={{ textAlign: 'right' }}>
            <Typography variant="body2" fontWeight={600}>
              {ing.stock} {ing.unite}
            </Typography>
            <Typography variant="caption" color={canMakePortions > 0 ? 'text.secondary' : 'error.main'}>
              (~{canMakePortions} portion{canMakePortions > 1 ? 's' : ''})
            </Typography>
          </Box>
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
            color="primary"
            onClick={() => {
              setEditingRecipeLine(row);
              setIsFormOpen(true);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color="error"
            onClick={() => setDeletingRecipeLine(row)}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  const handleFormSubmit = (data) => {
    if (editingRecipeLine) {
      updateMutation.mutate({ id: editingRecipeLine.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Fiches Recettes & Ingrédients 📖
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Configurez la liste des ingrédients et quantités nécessaires pour confectionner chaque plat.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          disabled={!selectedDishId}
          onClick={() => {
            setEditingRecipeLine(null);
            setIsFormOpen(true);
          }}
          sx={{ py: 1.2, px: 2.5 }}
        >
          Ajouter un ingrédient
        </Button>
      </Box>

      {/* Dish Selector card */}
      <Card sx={{ p: 3, borderRadius: 3, border: '1px solid #f1f5f9' }}>
        <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
          <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, alignItems: { xs: 'flex-start', md: 'center' }, gap: 3 }}>
            <Box sx={{ minWidth: 300 }}>
              <TextField
                select
                fullWidth
                label="Sélectionner le plat à consulter ou modifier"
                value={selectedDishId}
                onChange={(e) => setSelectedDishId(e.target.value)}
              >
                {dishes.map((dish) => (
                  <MenuItem key={dish.id} value={dish.id}>
                    {dish.nom} ({dish.categorie})
                  </MenuItem>
                ))}
              </TextField>
            </Box>

            {currentDish && (
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                <Typography variant="body2" color="text.secondary">
                  Prix de vente : <strong>{currentDish.prix.toFixed(2)} €</strong>
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Catégorie : <strong>{currentDish.categorie}</strong>
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Statut : <strong>{currentDish.disponible ? 'Disponible' : 'Indisponible'}</strong>
                </Typography>
              </Box>
            )}
          </Box>
        </CardContent>
      </Card>

      {/* Recipe lines table */}
      {loadingDishes || loadingRecipes ? (
        <LoadingSpinner message="Chargement de la fiche recette..." />
      ) : (
        <DataTable
          columns={columns}
          rows={recipeLines}
          loading={loadingRecipes}
          emptyMessage="Aucun ingrédient associé à ce plat pour le moment. Cliquez sur 'Ajouter un ingrédient'."
        />
      )}

      {/* Add / Edit RecipeLine Dialog */}
      <RecipeLineForm
        open={isFormOpen}
        dishId={selectedDishId}
        recipeLine={editingRecipeLine}
        ingredients={ingredients}
        loading={createMutation.isPending || updateMutation.isPending}
        onClose={() => {
          setIsFormOpen(false);
          setEditingRecipeLine(null);
        }}
        onSubmit={handleFormSubmit}
      />

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={Boolean(deletingRecipeLine)}
        title="Retirer cet ingrédient ?"
        message={`Retirer "${deletingRecipeLine?.ingredientNom}" de la recette ?`}
        confirmLabel="Retirer"
        loading={deleteMutation.isPending}
        onClose={() => setDeletingRecipeLine(null)}
        onConfirm={() => deleteMutation.mutate(deletingRecipeLine.id)}
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


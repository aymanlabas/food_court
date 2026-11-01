import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  MenuItem,
  Typography,
  Box,
} from '@mui/material';

export default function RecipeLineForm({
  open,
  onClose,
  onSubmit,
  dishId,
  recipeLine = null,
  ingredients = [],
  loading = false,
}) {
  const [formData, setFormData] = useState({
    ingredientId: '',
    quantite: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (recipeLine) {
      setFormData({
        ingredientId: recipeLine.ingredientId || '',
        quantite: recipeLine.quantite !== undefined ? recipeLine.quantite : '',
      });
    } else {
      setFormData({
        ingredientId: '',
        quantite: '',
      });
    }
    setErrors({});
  }, [recipeLine, open]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const selectedIngredient = ingredients.find((i) => i.id === Number(formData.ingredientId));

  const validate = () => {
    const newErrors = {};
    if (!formData.ingredientId) newErrors.ingredientId = "Sélectionnez un ingrédient";
    if (formData.quantite === '' || isNaN(formData.quantite) || Number(formData.quantite) <= 0) {
      newErrors.quantite = 'La quantité requise doit être strictement positive';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit({
      dishId: Number(dishId),
      ingredientId: Number(formData.ingredientId),
      quantite: parseFloat(formData.quantite),
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <form onSubmit={handleSubmit}>
        <DialogTitle fontWeight={700}>
          {recipeLine ? 'Modifier l’ingrédient de la recette' : 'Ajouter un ingrédient à la recette'}
        </DialogTitle>
        <DialogContent dividers sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, pt: 2 }}>
          <TextField
            name="ingredientId"
            label="Ingrédient"
            select
            value={formData.ingredientId}
            onChange={handleChange}
            error={Boolean(errors.ingredientId)}
            helperText={errors.ingredientId}
            fullWidth
            required
            disabled={Boolean(recipeLine)}
          >
            {ingredients.map((ing) => (
              <MenuItem key={ing.id} value={ing.id}>
                {ing.nom} (Stock actuel: {ing.stock} {ing.unite})
              </MenuItem>
            ))}
          </TextField>

          <TextField
            name="quantite"
            label={`Quantité requise par portion ${selectedIngredient ? `(${selectedIngredient.unite})` : ''}`}
            type="number"
            inputProps={{ step: '0.001', min: '0.001' }}
            value={formData.quantite}
            onChange={handleChange}
            error={Boolean(errors.quantite)}
            helperText={errors.quantite}
            fullWidth
            required
          />

          {selectedIngredient && (
            <Box sx={{ p: 2, bgcolor: '#f8fafc', borderRadius: 2, border: '1px solid #e2e8f0' }}>
              <Typography variant="caption" color="text.secondary" display="block">
                Unité : <strong>{selectedIngredient.unite}</strong>
              </Typography>
              <Typography variant="caption" color="text.secondary" display="block">
                Stock disponible en cuisine : <strong>{selectedIngredient.stock} {selectedIngredient.unite}</strong>
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={onClose} color="inherit" disabled={loading}>
            Annuler
          </Button>
          <Button type="submit" variant="contained" color="primary" disabled={loading}>
            {loading ? 'Enregistrement...' : recipeLine ? 'Mettre à jour' : 'Ajouter'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}


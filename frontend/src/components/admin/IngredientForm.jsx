import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  MenuItem,
} from '@mui/material';

const commonUnits = ['kg', 'g', 'L', 'cL', 'unité'];

export default function IngredientForm({ open, onClose, onSubmit, ingredient = null, loading = false }) {
  const [formData, setFormData] = useState({
    nom: '',
    stock: '',
    unite: 'kg',
    seuilAlerte: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (ingredient) {
      setFormData({
        nom: ingredient.nom || '',
        stock: ingredient.stock !== undefined ? ingredient.stock : '',
        unite: ingredient.unite || 'kg',
        seuilAlerte: ingredient.seuilAlerte !== undefined ? ingredient.seuilAlerte : '',
      });
    } else {
      setFormData({
        nom: '',
        stock: '',
        unite: 'kg',
        seuilAlerte: '',
      });
    }
    setErrors({});
  }, [ingredient, open]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.nom.trim()) newErrors.nom = "Le nom de l'ingrédient est requis";
    if (formData.stock === '' || isNaN(formData.stock) || Number(formData.stock) < 0) {
      newErrors.stock = 'Le stock doit être un nombre positif ou nul';
    }
    if (!formData.unite) newErrors.unite = "L'unité est requise";
    if (formData.seuilAlerte === '' || isNaN(formData.seuilAlerte) || Number(formData.seuilAlerte) < 0) {
      newErrors.seuilAlerte = "Le seuil d'alerte doit être un nombre positif ou nul";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit({
      ...formData,
      stock: parseFloat(formData.stock),
      seuilAlerte: parseFloat(formData.seuilAlerte),
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <form onSubmit={handleSubmit}>
        <DialogTitle fontWeight={700}>
          {ingredient ? "Modifier l'ingrédient" : "Ajouter un ingrédient"}
        </DialogTitle>
        <DialogContent dividers sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, pt: 2 }}>
          <TextField
            name="nom"
            label="Nom de l'ingrédient"
            value={formData.nom}
            onChange={handleChange}
            error={Boolean(errors.nom)}
            helperText={errors.nom}
            fullWidth
            required
          />

          <TextField
            name="unite"
            label="Unité de mesure"
            select
            value={formData.unite}
            onChange={handleChange}
            error={Boolean(errors.unite)}
            helperText={errors.unite}
            fullWidth
            required
          >
            {commonUnits.map((u) => (
              <MenuItem key={u} value={u}>
                {u}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            name="stock"
            label="Quantité en stock"
            type="number"
            inputProps={{ step: '0.001', min: '0' }}
            value={formData.stock}
            onChange={handleChange}
            error={Boolean(errors.stock)}
            helperText={errors.stock}
            fullWidth
            required
          />

          <TextField
            name="seuilAlerte"
            label="Seuil d'alerte stock bas"
            type="number"
            inputProps={{ step: '0.001', min: '0' }}
            value={formData.seuilAlerte}
            onChange={handleChange}
            error={Boolean(errors.seuilAlerte)}
            helperText={errors.seuilAlerte}
            fullWidth
            required
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={onClose} color="inherit" disabled={loading}>
            Annuler
          </Button>
          <Button type="submit" variant="contained" color="primary" disabled={loading}>
            {loading ? 'Enregistrement...' : ingredient ? 'Mettre à jour' : 'Ajouter'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}


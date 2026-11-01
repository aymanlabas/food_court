import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  MenuItem,
  FormControlLabel,
  Switch,
  Box,
} from '@mui/material';
import { CATEGORIES } from '../../utils/constants';

const availableCategories = CATEGORIES.filter((c) => c.value !== 'ALL');

export default function DishForm({ open, onClose, onSubmit, dish = null, loading = false }) {
  const [formData, setFormData] = useState({
    nom: '',
    description: '',
    prix: '',
    categorie: 'PLAT_PRINCIPAL',
    allergenes: '',
    disponible: true,
    imageUrl: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (dish) {
      setFormData({
        nom: dish.nom || '',
        description: dish.description || '',
        prix: dish.prix || '',
        categorie: dish.categorie || 'PLAT_PRINCIPAL',
        allergenes: dish.allergenes || '',
        disponible: dish.disponible !== undefined ? dish.disponible : true,
        imageUrl: dish.imageUrl || '',
      });
    } else {
      setFormData({
        nom: '',
        description: '',
        prix: '',
        categorie: 'PLAT_PRINCIPAL',
        allergenes: '',
        disponible: true,
        imageUrl: '',
      });
    }
    setErrors({});
  }, [dish, open]);

  const handleChange = (e) => {
    const { name, value, checked, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.nom.trim()) newErrors.nom = 'Le nom du plat est requis';
    if (!formData.prix || isNaN(formData.prix) || Number(formData.prix) <= 0) {
      newErrors.prix = 'Le prix doit être un nombre positif';
    }
    if (!formData.categorie) newErrors.categorie = 'La catégorie est requise';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit({
      ...formData,
      prix: parseFloat(formData.prix),
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit}>
        <DialogTitle fontWeight={700}>
          {dish ? 'Modifier le plat' : 'Ajouter un nouveau plat'}
        </DialogTitle>
        <DialogContent dividers sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, pt: 2 }}>
          <TextField
            name="nom"
            label="Nom du plat"
            value={formData.nom}
            onChange={handleChange}
            error={Boolean(errors.nom)}
            helperText={errors.nom}
            fullWidth
            required
          />

          <TextField
            name="categorie"
            label="Catégorie"
            select
            value={formData.categorie}
            onChange={handleChange}
            error={Boolean(errors.categorie)}
            helperText={errors.categorie}
            fullWidth
            required
          >
            {availableCategories.map((cat) => (
              <MenuItem key={cat.value} value={cat.value}>
                {cat.label}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            name="prix"
            label="Prix (€)"
            type="number"
            inputProps={{ step: '0.05', min: '0' }}
            value={formData.prix}
            onChange={handleChange}
            error={Boolean(errors.prix)}
            helperText={errors.prix}
            fullWidth
            required
          />

          <TextField
            name="description"
            label="Description"
            multiline
            rows={3}
            value={formData.description}
            onChange={handleChange}
            fullWidth
          />

          <TextField
            name="allergenes"
            label="Allergènes (séparés par des virgules)"
            placeholder="Gluten, Lait, Oeufs"
            value={formData.allergenes}
            onChange={handleChange}
            fullWidth
          />

          <TextField
            name="imageUrl"
            label="URL de l'image (optionnel)"
            placeholder="https://images.unsplash.com/..."
            value={formData.imageUrl}
            onChange={handleChange}
            fullWidth
          />

          <FormControlLabel
            control={
              <Switch
                name="disponible"
                checked={formData.disponible}
                onChange={handleChange}
                color="primary"
              />
            }
            label="Disponible à la commande"
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={onClose} color="inherit" disabled={loading}>
            Annuler
          </Button>
          <Button type="submit" variant="contained" color="primary" disabled={loading}>
            {loading ? 'Enregistrement...' : dish ? 'Mettre à jour' : 'Créer le plat'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}


import React from 'react';
import { Box, Chip } from '@mui/material';
import { CATEGORIES } from '../../utils/constants';

export default function CategoryFilter({ selectedCategory = 'ALL', onSelectCategory }) {
  return (
    <Box
      sx={{
        display: 'flex',
        gap: 1,
        overflowX: 'auto',
        pb: 1,
        '::-webkit-scrollbar': { height: 4 },
        '::-webkit-scrollbar-thumb': { backgroundColor: '#cbd5e1', borderRadius: 4 },
      }}
    >
      {CATEGORIES.map((cat) => {
        const isSelected = selectedCategory === cat.value;
        return (
          <Chip
            key={cat.value}
            label={cat.label}
            clickable
            onClick={() => onSelectCategory(cat.value)}
            sx={{
              fontWeight: isSelected ? 700 : 500,
              fontSize: '0.85rem',
              px: 1,
              py: 2.2,
              borderRadius: 3,
              backgroundColor: isSelected ? '#16a34a' : '#ffffff',
              color: isSelected ? '#ffffff' : '#475569',
              border: '1px solid',
              borderColor: isSelected ? '#16a34a' : '#e2e8f0',
              '&:hover': {
                backgroundColor: isSelected ? '#15803d' : '#f8fafc',
              },
            }}
          />
        );
      })}
    </Box>
  );
}


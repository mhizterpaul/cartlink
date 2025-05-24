import React, { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { TextField, Select, MenuItem, FormControl, InputLabel, Button, Grid, Typography, Box, IconButton } from '@mui/material';
import { GoogleGenerativeAI } from '@google/generative-ai';
import CloseIcon from '@mui/icons-material/Close';

interface ProductFormProps {
    onClose?: () => void;
}

interface ProductFormData {
    name: string;
    manufacturer: string;
    productionYear: number;
    price: number;
    unitsInStock: number;
    type: 'electronics' | 'fashion' | 'cosmetics';
    description: string;
    payOnDelivery: boolean;
    images: File[];
    videos: File[];
    imported: boolean;

    // Electronics specific fields
    dimensions?: {
        length: number;
        width: number;
        height: number;
    };
    model?: string;
    spec?: string;
    warranty?: string;
    marketVariant?: string;
    powerRating?: string;
    electronicsType?: string;

    // Fashion specific fields
    sex?: string;
    fashionType?: string;
    size?: string;
    material?: string;

    // Cosmetics specific fields
    skinType?: string;
    ingredients?: string;
    applicationDirection?: string;
}

const ProductForm: React.FC<ProductFormProps> = ({ onClose }) => {
    const { control, handleSubmit, watch } = useForm<ProductFormData>();
    const [specLoading, setSpecLoading] = useState(false);
    const productType = watch('type');

    const fetchSpecFromGemini = async (model: string) => {
        try {
            setSpecLoading(true);
            const genAI = new GoogleGenerativeAI(process.env.VITE_GEMINI_API_KEY || '');
            const model = genAI.getGenerativeModel({ model: "gemini-pro" });

            const prompt = `Please provide technical specifications for the following product model: ${model}. Include key features, dimensions, and technical details.`;
            const result = await model.generateContent(prompt);
            const response = await result.response;
            const spec = response.text();

            return spec;
        } catch (error) {
            console.error('Error fetching spec from Gemini:', error);
            return null;
        } finally {
            setSpecLoading(false);
        }
    };

    const onSubmit = async (data: ProductFormData) => {
        if (data.type === 'electronics' && data.model) {
            const spec = await fetchSpecFromGemini(data.model);
            if (spec) {
                data.spec = spec;
            }
        }
        // Handle form submission
        console.log(data);
        onClose?.();
    };

    return (
        <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ p: 3, position: 'relative' }}>
            <IconButton
                onClick={onClose}
                sx={{
                    position: 'absolute',
                    right: 8,
                    top: 8,
                }}
            >
                <CloseIcon />
            </IconButton>

            <Typography variant="h5" gutterBottom>
                Add New Product
            </Typography>

            <Grid container spacing={3}>
                {/* Common Fields */}
                <Grid item xs={12} md={6}>
                    <Controller
                        name="name"
                        control={control}
                        rules={{ required: 'Name is required' }}
                        render={({ field, fieldState: { error } }) => (
                            <TextField
                                {...field}
                                label="Product Name"
                                fullWidth
                                error={!!error}
                                helperText={error?.message}
                            />
                        )}
                    />
                </Grid>

                <Grid item xs={12} md={6}>
                    <Controller
                        name="manufacturer"
                        control={control}
                        rules={{ required: 'Manufacturer is required' }}
                        render={({ field, fieldState: { error } }) => (
                            <TextField
                                {...field}
                                label="Manufacturer"
                                fullWidth
                                error={!!error}
                                helperText={error?.message}
                            />
                        )}
                    />
                </Grid>

                <Grid item xs={12} md={6}>
                    <Controller
                        name="price"
                        control={control}
                        rules={{ required: 'Price is required' }}
                        render={({ field, fieldState: { error } }) => (
                            <TextField
                                {...field}
                                label="Price"
                                type="number"
                                fullWidth
                                error={!!error}
                                helperText={error?.message}
                            />
                        )}
                    />
                </Grid>

                <Grid item xs={12} md={6}>
                    <Controller
                        name="unitsInStock"
                        control={control}
                        rules={{ required: 'Units in stock is required' }}
                        render={({ field, fieldState: { error } }) => (
                            <TextField
                                {...field}
                                label="Units in Stock"
                                type="number"
                                fullWidth
                                error={!!error}
                                helperText={error?.message}
                            />
                        )}
                    />
                </Grid>

                <Grid item xs={12} md={6}>
                    <Controller
                        name="type"
                        control={control}
                        rules={{ required: 'Product type is required' }}
                        render={({ field, fieldState: { error } }) => (
                            <FormControl fullWidth error={!!error}>
                                <InputLabel>Product Type</InputLabel>
                                <Select {...field} label="Product Type">
                                    <MenuItem value="electronics">Electronics</MenuItem>
                                    <MenuItem value="fashion">Fashion</MenuItem>
                                    <MenuItem value="cosmetics">Cosmetics</MenuItem>
                                </Select>
                            </FormControl>
                        )}
                    />
                </Grid>

                {/* Type-specific fields */}
                {productType === 'electronics' && (
                    <>
                        <Grid item xs={12} md={6}>
                            <Controller
                                name="model"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Model"
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <Controller
                                name="electronicsType"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Type (e.g., smartphone)"
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Controller
                                name="spec"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Specifications"
                                        multiline
                                        rows={4}
                                        fullWidth
                                        disabled={specLoading}
                                        helperText={specLoading ? "Fetching specifications..." : "Specifications will be fetched automatically if model is provided"}
                                    />
                                )}
                            />
                        </Grid>
                    </>
                )}

                {productType === 'fashion' && (
                    <>
                        <Grid item xs={12} md={6}>
                            <Controller
                                name="sex"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Sex"
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <Controller
                                name="fashionType"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Type (e.g., shoe)"
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <Controller
                                name="size"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Size"
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <Controller
                                name="material"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Material"
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                    </>
                )}

                {productType === 'cosmetics' && (
                    <>
                        <Grid item xs={12} md={6}>
                            <Controller
                                name="skinType"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Skin Type"
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <Controller
                                name="ingredients"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Ingredients"
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Controller
                                name="applicationDirection"
                                control={control}
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        label="Application Direction"
                                        multiline
                                        rows={4}
                                        fullWidth
                                    />
                                )}
                            />
                        </Grid>
                    </>
                )}

                {/* Common fields continued */}
                <Grid item xs={12}>
                    <Controller
                        name="description"
                        control={control}
                        rules={{ required: 'Description is required' }}
                        render={({ field, fieldState: { error } }) => (
                            <TextField
                                {...field}
                                label="Description"
                                multiline
                                rows={4}
                                fullWidth
                                error={!!error}
                                helperText={error?.message}
                            />
                        )}
                    />
                </Grid>

                <Grid item xs={12}>
                    <Button
                        type="submit"
                        variant="contained"
                        color="primary"
                        size="large"
                        fullWidth
                    >
                        Add Product
                    </Button>
                </Grid>
            </Grid>
        </Box>
    );
};

export default ProductForm; 
import React, { useState } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    TextField,
    Box,
    Typography,
    IconButton,
    Snackbar,
    Alert,
} from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import { merchantApi } from '../../../../services/api';

interface CreateLinkDialogProps {
    open: boolean;
    onClose: () => void;
    productId: number;
    productName: string;
}

export default function CreateLinkDialog({ open, onClose, productId, productName }: CreateLinkDialogProps) {
    const [generatedLink, setGeneratedLink] = useState<string>('');
    const [isLoading, setIsLoading] = useState(false);
    const [showCopiedAlert, setShowCopiedAlert] = useState(false);

    const handleCreateLink = async () => {
        try {
            setIsLoading(true);
            const response = await merchantApi.createProductLink(productId);
            const baseUrl = window.location.origin;
            setGeneratedLink(`${baseUrl}/product/${response.data.linkId}`);
        } catch (error) {
            console.error('Error creating link:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleCopyLink = () => {
        navigator.clipboard.writeText(generatedLink);
        setShowCopiedAlert(true);
    };

    return (
        <>
            <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
                <DialogTitle>Create Product Link</DialogTitle>
                <DialogContent>
                    <Box sx={{ mb: 3 }}>
                        <Typography variant="subtitle1" gutterBottom>
                            Product: {productName}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Create a unique link to share this product. When customers click this link,
                            they'll be taken to the product page and their visit will be tracked.
                        </Typography>
                    </Box>

                    {generatedLink ? (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <TextField
                                fullWidth
                                value={generatedLink}
                                InputProps={{
                                    readOnly: true,
                                }}
                                size="small"
                            />
                            <IconButton onClick={handleCopyLink} color="primary">
                                <ContentCopyIcon />
                            </IconButton>
                        </Box>
                    ) : (
                        <Button
                            variant="contained"
                            onClick={handleCreateLink}
                            disabled={isLoading}
                            fullWidth
                        >
                            {isLoading ? 'Creating Link...' : 'Generate Link'}
                        </Button>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={onClose}>Close</Button>
                </DialogActions>
            </Dialog>

            <Snackbar
                open={showCopiedAlert}
                autoHideDuration={3000}
                onClose={() => setShowCopiedAlert(false)}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert severity="success" onClose={() => setShowCopiedAlert(false)}>
                    Link copied to clipboard!
                </Alert>
            </Snackbar>
        </>
    );
} 
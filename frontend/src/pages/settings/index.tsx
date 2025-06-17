import React, { useState, useEffect } from "react";
import {
    Box,
    Typography,
    TextField,
    Grid,
    IconButton,
    Avatar,
    Button,
    MenuItem,
    Paper,
    Divider,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import { auth } from '../../api';

const months = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
];

interface MerchantProfile {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber?: string;
    profileImage?: string;
    [key: string]: any;
}

export default function AccountInfo() {
    const [merchant, setMerchant] = useState<MerchantProfile | null>(null);
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [phone, setPhone] = useState("");
    const [email, setEmail] = useState("");
    const [birthDay, setBirthDay] = useState("");
    const [birthMonth, setBirthMonth] = useState("");
    const [profileImage, setProfileImage] = useState<string | ArrayBuffer | null>(null);

    useEffect(() => {
        const fetchMerchantProfile = async () => {
            try {
                const profile = await auth.merchant.getProfile();
                setMerchant(profile);
                setFirstName(profile.firstName || '');
                setLastName(profile.lastName || '');
                setPhone(profile.phoneNumber || '');
                setEmail(profile.email || '');
                if (profile.profileImage) {
                    setProfileImage(profile.profileImage);
                }
            } catch (error) {
                console.error('Error fetching merchant profile:', error);
            }
        };
        fetchMerchantProfile();
    }, []);

    const handleImageUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = () => {
                setProfileImage(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSave = async () => {
        try {
            const updatedProfile = {
                firstName,
                lastName,
                phoneNumber: phone,
                profileImage: profileImage as string
            };
            const response = await auth.merchant.updateProfile(updatedProfile);
            setMerchant(response.data as MerchantProfile);
            // Show success message or handle response
        } catch (error) {
            console.error('Error updating merchant profile:', error);
            // Show error message
        }
    };

    if (!merchant) return null;

    return (
        <Paper elevation={0} sx={{ p: 4, maxWidth: 900, mx: "auto" }}>
            <Box display="flex" alignItems="center" mb={4}>
                <Box position="relative" mr={2}>
                    <Avatar src={profileImage as string} sx={{ width: 64, height: 64 }} />
                    <input
                        accept="image/*"
                        id="upload-avatar"
                        type="file"
                        style={{ display: "none" }}
                        onChange={handleImageUpload}
                    />
                    <label htmlFor="upload-avatar">
                        <IconButton
                            component="span"
                            sx={{
                                position: "absolute",
                                bottom: -8,
                                right: -8,
                                backgroundColor: "background.paper",
                                boxShadow: 1,
                            }}
                        >
                            <EditIcon fontSize="small" />
                        </IconButton>
                    </label>
                </Box>
                <Typography variant="h5">Hello, {firstName}!</Typography>
            </Box>

            <Typography variant="subtitle1" gutterBottom>
                Manage your profile and preferences here.
            </Typography>

            <Divider sx={{ my: 3 }} />

            <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                    <TextField
                        fullWidth
                        label="First Name"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                    />
                </Grid>
                <Grid item xs={12} sm={6}>
                    <TextField
                        fullWidth
                        label="Last Name"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                    />
                </Grid>
                <Grid item xs={12} sm={6}>
                    <TextField
                        fullWidth
                        label="Phone Number"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                    />
                </Grid>
                <Grid item xs={6} sm={3}>
                    <TextField
                        select
                        fullWidth
                        label="Day"
                        value={birthDay}
                        onChange={(e) => setBirthDay(e.target.value)}
                    >
                        {[...Array(31)].map((_, i) => (
                            <MenuItem key={i + 1} value={String(i + 1)}>
                                {i + 1}
                            </MenuItem>
                        ))}
                    </TextField>
                </Grid>
                <Grid item xs={6} sm={3}>
                    <TextField
                        select
                        fullWidth
                        label="Month"
                        value={birthMonth}
                        onChange={(e) => setBirthMonth(e.target.value)}
                    >
                        {months.map((month, i) => (
                            <MenuItem key={i} value={month}>
                                {month}
                            </MenuItem>
                        ))}
                    </TextField>
                </Grid>
                <Grid item xs={12} sm={6}>
                    <TextField fullWidth label="Email" value={email} disabled />
                </Grid>
                <Grid item xs={12} sm={6}>
                    <Box display="flex" alignItems="center" height="100%">
                        <Typography sx={{ mr: 2 }}>Password</Typography>
                        <Button size="small">Change</Button>
                    </Box>
                </Grid>
            </Grid>

            <Box sx={{ mt: 4, display: 'flex', justifyContent: 'flex-end' }}>
                <Button variant="contained" onClick={handleSave}>
                    Save Changes
                </Button>
            </Box>
        </Paper>
    );
}

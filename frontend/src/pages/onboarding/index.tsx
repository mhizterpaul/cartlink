import * as React from 'react';
import {
  Box,
  Typography,
  Stepper,
  Step,
  StepLabel,
  Button,
  Paper,
  useTheme,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import StorefrontIcon from '@mui/icons-material/Storefront';
import ListAltIcon from '@mui/icons-material/ListAlt';
import onboardingMockup from './mockup/3c98ee317e7daebfc09c8afc413997f56569a052.jpg';

const steps = [
  'Welcome',
  'Profile',
  'Stores',
  'First List',
];

const stepIcons = [
  <CheckCircleIcon fontSize="large" color="primary" />, // Welcome
  <PersonAddAltIcon fontSize="large" color="primary" />, // Profile
  <StorefrontIcon fontSize="large" color="primary" />, // Stores
  <ListAltIcon fontSize="large" color="primary" />, // First List
];

const OnboardingContainer = styled(Paper)(({ theme }) => ({
  margin: 'auto',
  marginTop: theme.spacing(8),
  padding: theme.spacing(5, 4),
  maxWidth: 480,
  borderRadius: 24,
  boxShadow: '0 8px 32px 0 rgba(60,72,100,0.08)',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  background: theme.palette.background.paper,
}));

const StepIconBox = styled(Box)(({ theme }) => ({
  width: 56,
  height: 56,
  borderRadius: '50%',
  background: theme.palette.primary.light,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  margin: 'auto',
  marginBottom: theme.spacing(2),
}));

const StepContent = styled(Box)(({ theme }) => ({
  marginTop: theme.spacing(4),
  marginBottom: theme.spacing(4),
  textAlign: 'center',
}));

const ButtonContainer = styled(Box)(({ theme }) => ({
  marginTop: theme.spacing(4),
  display: 'flex',
  justifyContent: 'space-between',
  width: '100%',
}));

const Illustration = styled('img')({
  width: 180,
  height: 120,
  objectFit: 'contain',
  margin: '24px auto 0',
  display: 'block',
});

export default function OnboardingPage() {
  const [activeStep, setActiveStep] = React.useState(0);
  const theme = useTheme();

  const handleNext = () => {
    setActiveStep((prevStep) => prevStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevStep) => prevStep - 1);
  };

  const handleFinish = () => {
    // Handle completion of onboarding
    console.log('Onboarding completed');
  };

  const getStepContent = (step: number) => {
    switch (step) {
      case 0:
        return (
          <>
            <StepIconBox>{stepIcons[0]}</StepIconBox>
            <Typography variant="h4" fontWeight={700} gutterBottom>
              Welcome to CartLink!
            </Typography>
            <Typography variant="body1" color="text.secondary" paragraph>
              Your ultimate shopping companion for managing lists, comparing prices, and finding the best deals.
            </Typography>
            <Illustration src={onboardingMockup} alt="Welcome Illustration" />
          </>
        );
      case 1:
        return (
          <>
            <StepIconBox>{stepIcons[1]}</StepIconBox>
            <Typography variant="h4" fontWeight={700} gutterBottom>
              Set Up Your Profile
            </Typography>
            <Typography variant="body1" color="text.secondary" paragraph>
              Tell us a bit about yourself to personalize your shopping experience.
            </Typography>
            <Illustration src={onboardingMockup} alt="Profile Illustration" />
          </>
        );
      case 2:
        return (
          <>
            <StepIconBox>{stepIcons[2]}</StepIconBox>
            <Typography variant="h4" fontWeight={700} gutterBottom>
              Connect Your Stores
            </Typography>
            <Typography variant="body1" color="text.secondary" paragraph>
              Link your favorite stores to get real-time price updates and deals.
            </Typography>
            <Illustration src={onboardingMockup} alt="Stores Illustration" />
          </>
        );
      case 3:
        return (
          <>
            <StepIconBox>{stepIcons[3]}</StepIconBox>
            <Typography variant="h4" fontWeight={700} gutterBottom>
              Create Your First List
            </Typography>
            <Typography variant="body1" color="text.secondary" paragraph>
              Start organizing your shopping with a new list.
            </Typography>
            <Illustration src={onboardingMockup} alt="List Illustration" />
          </>
        );
      default:
        return 'Unknown step';
    }
  };

  return (
    <Box sx={{ width: '100%', minHeight: '100vh', bgcolor: 'background.default', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <OnboardingContainer elevation={3}>
        <Stepper activeStep={activeStep} alternativeLabel sx={{ width: '100%', mb: 3 }}>
          {steps.map((label, idx) => (
            <Step key={label} completed={activeStep > idx}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        <StepContent>
          {getStepContent(activeStep)}
        </StepContent>
        <ButtonContainer>
          <Button
            disabled={activeStep === 0}
            onClick={handleBack}
            variant="outlined"
            sx={{ borderRadius: 2, px: 4 }}
          >
            Back
          </Button>
          {activeStep === steps.length - 1 ? (
            <Button
              variant="contained"
              color="primary"
              onClick={handleFinish}
              sx={{ borderRadius: 2, px: 4 }}
            >
              Finish
            </Button>
          ) : (
            <Button
              variant="contained"
              color="primary"
              onClick={handleNext}
              sx={{ borderRadius: 2, px: 4 }}
            >
              Next
            </Button>
          )}
        </ButtonContainer>
      </OnboardingContainer>
    </Box>
  );
} 
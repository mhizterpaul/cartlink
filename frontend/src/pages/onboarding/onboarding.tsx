import * as React from 'react';
import { Box, Stepper, Step, StepLabel, Button, Typography, Paper } from '@mui/material';
import { styled } from '@mui/material/styles';
import BaseAppBar from '@/pages/components/base-appbar';

const steps = [
  'Welcome to CartLink',
  'Set Up Your Profile',
  'Connect Your Stores',
  'Create Your First List',
];

const OnboardingContainer = styled(Paper)(({ theme }) => ({
  marginTop: theme.spacing(8),
  padding: theme.spacing(4),
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
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

export default function OnboardingPage() {
  const [activeStep, setActiveStep] = React.useState(0);

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
            <Typography variant="h4" gutterBottom>
              Welcome to CartLink!
            </Typography>
            <Typography variant="body1" paragraph>
              Your ultimate shopping companion that helps you manage lists, compare prices, and find the best deals.
            </Typography>
          </>
        );
      case 1:
        return (
          <>
            <Typography variant="h4" gutterBottom>
              Set Up Your Profile
            </Typography>
            <Typography variant="body1" paragraph>
              Tell us a bit about yourself to personalize your shopping experience.
            </Typography>
          </>
        );
      case 2:
        return (
          <>
            <Typography variant="h4" gutterBottom>
              Connect Your Stores
            </Typography>
            <Typography variant="body1" paragraph>
              Link your favorite stores to get real-time price updates and deals.
            </Typography>
          </>
        );
      case 3:
        return (
          <>
            <Typography variant="h4" gutterBottom>
              Create Your First List
            </Typography>
            <Typography variant="body1" paragraph>
              Start organizing your shopping with a new list.
            </Typography>
          </>
        );
      default:
        return 'Unknown step';
    }
  };

  return (
    <Box sx={{ width: '100%' }}>
      <BaseAppBar title="CartLink" />
      <OnboardingContainer elevation={3}>
        <Stepper activeStep={activeStep} alternativeLabel>
          {steps.map((label) => (
            <Step key={label}>
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
          >
            Back
          </Button>
          {activeStep === steps.length - 1 ? (
            <Button
              variant="contained"
              color="primary"
              onClick={handleFinish}
            >
              Finish
            </Button>
          ) : (
            <Button
              variant="contained"
              color="primary"
              onClick={handleNext}
            >
              Next
            </Button>
          )}
        </ButtonContainer>
      </OnboardingContainer>
    </Box>
  );
} 
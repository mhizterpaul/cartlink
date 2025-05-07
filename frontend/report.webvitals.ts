// reportWebVitals.js
import { getCLS, getFID, getLCP, getFCP, getTTFB } from 'web-vitals';

function reportWebVitals(onPerfEntry) {
  if (onPerfEntry && typeof onPerfEntry === 'function') {
    getCLS(onPerfEntry);  // Measures visual stability
    getFID(onPerfEntry);  // Measures interactivity
    getLCP(onPerfEntry);  // Measures loading performance
    getFCP(onPerfEntry);  // Measures time to first content display
    getTTFB(onPerfEntry); // Measures server response time
  }
}

export default reportWebVitals;
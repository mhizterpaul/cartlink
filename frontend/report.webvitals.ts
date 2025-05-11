// reportWebVitals.js
import { onCLS, onINP, onLCP, onFCP, onTTFB } from 'web-vitals';

function reportWebVitals(onPerfEntry) {
  if (onPerfEntry && typeof onPerfEntry === 'function') {
    onCLS(onPerfEntry);   // Measures visual stability
    onINP(onPerfEntry);   // Measures interactivity
    onLCP(onPerfEntry);   // Measures loading performance
    onFCP(onPerfEntry);   // Measures time to first content display
    onTTFB(onPerfEntry);  // Measures server response time
  }
}

export default reportWebVitals;
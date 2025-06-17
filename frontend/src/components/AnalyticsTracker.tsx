import React, { useEffect, useRef } from 'react';
import axios from 'axios';

interface AnalyticsTrackerProps {
    linkId: number;
    source?: string;
}

const AnalyticsTracker: React.FC<AnalyticsTrackerProps> = ({ linkId, source }) => {
    const startTime = useRef<number>(Date.now());
    const hasRecordedPageView = useRef<boolean>(false);

    useEffect(() => {
        // Record page view when component mounts
        if (!hasRecordedPageView.current) {
            recordPageView();
            hasRecordedPageView.current = true;
        }

        // Record time spent when component unmounts
        return () => {
            const timeSpent = Math.floor((Date.now() - startTime.current) / 1000);
            recordTimeSpent(timeSpent);
        };
    }, []);

    const recordPageView = async () => {
        try {
            await axios.post(`/api/analytics/pageview/${linkId}`, null, {
                params: { source }
            });
        } catch (error) {
            console.error('Error recording page view:', error);
        }
    };

    const recordTimeSpent = async (timeSpentSeconds: number) => {
        try {
            await axios.post(`/api/analytics/timespent/${linkId}`, null, {
                params: { timeSpentSeconds }
            });
        } catch (error) {
            console.error('Error recording time spent:', error);
        }
    };

    return null; // This component doesn't render anything
};

export default AnalyticsTracker; 
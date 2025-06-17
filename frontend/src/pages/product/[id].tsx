import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/router';
import AnalyticsTracker from '../../components/AnalyticsTracker';
import { merchantApi } from '../../services/api';

interface ProductData {
    id: number;
    linkId: number;
    name: string;
    description: string;
    price: number;
    image: string;
    // Add other product fields as needed
}

export default function ProductPage() {
    const router = useRouter();
    const { id } = router.query;
    const [productData, setProductData] = useState<ProductData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchProductData = async () => {
            if (!id) return;

            try {
                setIsLoading(true);
                const response = await merchantApi.getProductByLinkId(Number(id));
                setProductData(response.data);
            } catch (err) {
                setError('Failed to load product');
                console.error('Error fetching product:', err);
            } finally {
                setIsLoading(false);
            }
        };

        fetchProductData();
    }, [id]);

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (error || !productData) {
        return <div>{error || 'Product not found'}</div>;
    }

    return (
        <>
            <AnalyticsTracker
                linkId={productData.linkId}
                source={new URLSearchParams(window.location.search).get('source') || undefined}
            />
            <div>
                <h1>{productData.name}</h1>
                <img src={productData.image} alt={productData.name} />
                <p>{productData.description}</p>
                <p>Price: ${productData.price.toFixed(2)}</p>
                {/* Add other product details as needed */}
            </div>
        </>
    );
} 
import React from 'react';
import styles from './review-components.module.css';

const reviews = [
    {
        name: 'Julia, Berlin',
        date: 'Jan 11',
        rating: 5,
        title: 'Moved here from another insurance company',
        text: 'Great car insurance company! Efficient and reliable service. Quick claims processing and excellent customer support. Affordable premiums and a wide range of coverage options.'
    },
    {
        name: 'Kim, Frankfurt',
        date: 'Jan 11',
        rating: 3,
        title: 'Decent Car Insurance Company with Room for Improvement',
        text: 'Decent car insurance company. Average service and claims processing time. Customer support could be better. Premiums are somewhat affordable, but coverage options are limited.',
        response: {
            date: 'Jan 12',
            text: 'Hi Kim! We\'ve responded to your email. Please check your inbox, including the Spam folder. Thanks for your patience and regards, Soglasie Team.'
        }
    }
];

const ReviewComponents = () => {
    return (
        <div className={styles.background}>
            <div className={styles.container}>
                <div className={styles.summaryCard}>
                    <button className={styles.backBtn}>&#8592;</button>
                    <div className={styles.summaryHeader}>Reviews and ratings</div>
                    <div className={styles.ratingRow}>
                        <span className={styles.mainRating}>4,7</span>
                        <span className={styles.stars}>{renderStars(4.5)}</span>
                    </div>
                    <div className={styles.ratingCount}>Based on 565 ratings</div>
                    <div className={styles.subRatings}>
                        <SubRating label="Reliability" value={4.1} color="#F9A825" />
                        <SubRating label="Payout rating" value={4.3} color="#00C48C" />
                        <SubRating label="Positive solutions" value={4.1} color="#00C48C" />
                    </div>
                    <button className={styles.showSummaryBtn}>Show summary ▼</button>
                </div>
                <div className={styles.reviewsSection}>
                    <div className={styles.reviewsHeaderRow}>
                        <div className={styles.reviewsHeader}>Reviews <span className={styles.reviewCount}>153</span></div>
                        <div className={styles.filters}>
                            <select className={styles.filterSelect}>
                                <option>Verified</option>
                            </select>
                            <select className={styles.filterSelect}>
                                <option>All ratings</option>
                            </select>
                        </div>
                    </div>
                    <div className={styles.reviewList}>
                        {reviews.map((review, idx) => (
                            <div className={styles.reviewCard} key={idx}>
                                <div className={styles.reviewHeader}>
                                    <span className={styles.reviewName}>{review.name}</span>
                                    <span className={styles.reviewDate}>{review.date}</span>
                                </div>
                                <div className={styles.reviewStars}>{renderStars(review.rating)}</div>
                                <div className={styles.reviewTitle}>{review.title}</div>
                                <div className={styles.reviewText}>{review.text}</div>
                                {review.response && (
                                    <div className={styles.responseCard}>
                                        <div className={styles.responseDate}>{review.response.date}</div>
                                        <div className={styles.responseText}>{review.response.text}</div>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

function renderStars(rating: number) {
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    const stars = [];
    for (let i = 0; i < fullStars; i++) {
        stars.push(<span key={i} className={styles.star}>★</span>);
    }
    if (halfStar) {
        stars.push(<span key="half" className={styles.starHalf}>★</span>);
    }
    while (stars.length < 5) {
        stars.push(<span key={stars.length} className={styles.starEmpty}>★</span>);
    }
    return stars;
}

const SubRating = ({ label, value, color }: { label: string; value: number; color: string }) => (
    <div className={styles.subRatingRow}>
        <span className={styles.subRatingLabel}>{label}</span>
        <div className={styles.subRatingBarWrapper}>
            <div className={styles.subRatingBarBg} />
            <div className={styles.subRatingBar} style={{ width: `${(value / 5) * 100}%`, background: color }} />
        </div>
        <span className={styles.subRatingValue}>{value.toFixed(1)}</span>
    </div>
);

export default ReviewComponents; 
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { submitReview } from "../api/reviewsApi";
import "../styles/SubmitReview.css";

const MAX_IMAGES = 5;
const MAX_SIZE_MB = 64;
const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024;

function SubmitReviewPage() {
    const { taskId } = useParams();
    const navigate = useNavigate();

    const [text, setText] = useState("");
    const [rate, setRate] = useState(0);
    const [images, setImages] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const handleTextChange = (e) => setText(e.target.value);

    const handleRateChange = (newRate) => setRate(newRate);

    const handleImageChange = (e) => {
        const files = Array.from(e.target.files);

        if (images.length + files.length > MAX_IMAGES) {
            setError(`You can only upload up to ${MAX_IMAGES} images.`);
            return;
        }

        const newImages = [];
        let hasError = false;

        files.forEach((file) => {
            if (file.size > MAX_SIZE_BYTES) {
                setError(`Image "${file.name}" exceeds the ${MAX_SIZE_MB}MB limit.`);
                hasError = true;
                return;
            }

            const reader = new FileReader();
            reader.onloadend = () => {
                setImages((prev) => [
                    ...prev,
                    {
                        file,
                        preview: reader.result,
                        name: file.name,
                        type: file.type,
                    },
                ]);
            };
            reader.readAsDataURL(file);
        });

        if (!hasError) setError(null);
        // Reset input
        e.target.value = "";
    };

    const removeImage = (indexToRemove) => {
        setImages((prev) => prev.filter((_, index) => index !== indexToRemove));
    };

    const handleSubmit = async () => {
        if (!text.trim()) {
            setError("Please write a review.");
            return;
        }
        if (rate === 0) {
            setError("Please select a rating.");
            return;
        }

        setIsSubmitting(true);
        setError(null);

        const reviewImages = images.map((img) => ({
            imgName: img.name,
            format: img.type.split("/")[1] || "jpeg", // default to jpeg if unknown
            imgFile: img.preview.split(",")[1], // Remove data URL prefix
        }));

        const payload = {
            taskId: Number(taskId),
            text: text,
            rate: rate,
            reviewImages: reviewImages,
            // time is handled by backend usually, or new Timestamp(System.currentTimeMillis())
        };

        try {
            await submitReview(payload);
            // Success - navigate back or to confirmation
            navigate(-1); // Or to previous page
        } catch (err) {
            console.error("Failed to submit review:", err);

            // Extract error message from backend response
            let errorMessage = "Failed to submit review. Please try again.";

            if (err.response?.data) {
                // If backend returns a string error message
                if (typeof err.response.data === 'string') {
                    errorMessage = err.response.data;
                }
                // If backend returns an object with a message property
                else if (err.response.data.message) {
                    errorMessage = err.response.data.message;
                }
                // If backend returns an error property
                else if (err.response.data.error) {
                    errorMessage = err.response.data.error;
                }
            }
            // If there's a general error message
            else if (err.message) {
                errorMessage = err.message;
            }

            setError(errorMessage);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <main className="page">
            <div className="card" style={{ maxWidth: "800px", margin: "0 auto" }}>
                <h1 className="section-heading">Write a Review</h1>

                {error && <div className="alert alert-error">{error}</div>}

                <div className="review-form">
                    {/* Star Rating */}
                    <div className="form-field">
                        <label>Rating</label>
                        <div className="star-rating-input">
                            {[1, 2, 3, 4, 5].map((star) => (
                                <span
                                    key={star}
                                    className={star <= rate ? "filled" : "empty"}
                                    onClick={() => handleRateChange(star)}
                                >
                                    ★
                                </span>
                            ))}
                        </div>
                        <input
                            type="hidden"
                            name="rate"
                            value={rate}
                            required
                        />
                    </div>

                    {/* Review Text */}
                    <div className="form-field">
                        <label htmlFor="review-text">Review (Required)</label>
                        <textarea
                            id="review-text"
                            className="task-textarea"
                            placeholder="Share your experience (max 50 chars)..."
                            value={text}
                            onChange={(e) => {
                                // Limit to 50 characters as requested
                                if (e.target.value.length <= 50) {
                                    handleTextChange(e);
                                }
                            }}
                            maxLength={50}
                            required
                        />
                        <div style={{ textAlign: "right", fontSize: "0.85rem", color: text.length >= 50 ? "red" : "#6b7280" }}>
                            {text.length}/50 characters
                        </div>
                    </div>

                    {/* Image Upload */}
                    <div className="form-field">
                        <label>Photos (Optional, max 5)</label>
                        <input
                            type="file"
                            id="file-upload"
                            accept="image/*"
                            multiple
                            onChange={handleImageChange}
                            style={{ display: "none" }}
                            disabled={images.length >= MAX_IMAGES}
                        />
                        <label htmlFor="file-upload" className="image-upload-area">
                            <p>Click to add photos</p>
                            <p className="tasker-card__meta">Max size {MAX_SIZE_MB}MB per image</p>
                        </label>

                        <div className="image-preview-list">
                            {images.map((img, index) => (
                                <div key={index} className="image-preview-item">
                                    <img src={img.preview} alt={`Preview ${index}`} />
                                    <button
                                        type="button"
                                        className="image-remove-btn"
                                        onClick={() => removeImage(index)}
                                    >
                                        ×
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Buttons */}
                    <div className="form-actions">
                        <button
                            type="button"
                            className="btn btn-ghost"
                            onClick={() => navigate(-1)}
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            className="btn btn-primary"
                            onClick={handleSubmit}
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? "Submitting..." : "Submit"}
                        </button>
                    </div>
                </div>
            </div>
        </main>
    );
}

export default SubmitReviewPage;

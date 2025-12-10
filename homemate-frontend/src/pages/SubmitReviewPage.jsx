import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { submitReview, updateReview, getReviewByTask, deleteReviewImage } from "../api/reviewsApi";
import "../styles/SubmitReview.css";

const MAX_IMAGES = 5;
const MAX_SIZE_MB = 64;
const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024;

function SubmitReviewPage() {
    const { taskId } = useParams();
    const navigate = useNavigate();

    const [reviewId, setReviewId] = useState(null);
    const [text, setText] = useState("");
    const [rate, setRate] = useState(0);
    const [images, setImages] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [isForbidden, setIsForbidden] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [deletedImageIds, setDeletedImageIds] = useState([]);

    useEffect(() => {
        loadReview();
    }, [taskId]);

    const loadReview = async () => {
        try {
            const review = await getReviewByTask(taskId);
            if (review) {
                setReviewId(review.reviewId);
                setText(review.text);
                setRate(review.rate);

                if (review.reviewImages) {
                    const loadedImages = review.reviewImages.map(img => {
                        let preview = "";
                        if (img.imgFile) {
                            if (typeof img.imgFile === 'string') {
                                preview = img.imgFile.startsWith('data:')
                                    ? img.imgFile
                                    : `data:image/${img.format};base64,${img.imgFile}`;
                            } else if (Array.isArray(img.imgFile)) {
                                // Handle byte array
                                const binary = String.fromCharCode(...img.imgFile);
                                preview = `data:image/${img.format};base64,${btoa(binary)}`;
                            }
                        }

                        return {
                            id: img.imgId,
                            name: img.imgName,
                            type: `image/${img.format}`,
                            preview: preview,
                            file: null // No file object for existing images
                        };
                    });
                    setImages(loadedImages);
                }
            }
        } catch (err) {
            // If 404, it just means no review exists yet, which is fine
            if (err.status !== 404) {
                console.error("Failed to load review:", err);
            }
        } finally {
            setIsLoading(false);
        }
    };

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
        const imageToRemove = images[indexToRemove];
        // If it's an existing image (has an id), track it for deletion
        if (imageToRemove.id) {
            setDeletedImageIds((prev) => [...prev, imageToRemove.id]);
        }
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
            imgId: img.id, // Include ID for existing images
            imgName: img.name,
            format: img.type.split("/")[1] || "jpeg",
            imgFile: img.preview.split(",")[1], // Remove data URL prefix
        }));

        const payload = {
            taskId: Number(taskId),
            text: text,
            rate: rate,
            reviewImages: reviewImages,
        };

        if (reviewId) {
            payload.reviewId = reviewId;
        }

        try {
            // Delete removed images first
            for (const imageId of deletedImageIds) {
                try {
                    await deleteReviewImage(imageId);
                } catch (deleteErr) {
                    console.error(`Failed to delete image ${imageId}:`, deleteErr);
                    // Continue with other deletions even if one fails
                }
            }

            if (reviewId) {
                await updateReview(payload);
            } else {
                await submitReview(payload);
            }
            // Success - navigate to task details with success message
            navigate(`/tasks/${taskId}`, { state: { reviewSubmitted: true } });
        } catch (err) {
            console.error("Failed to submit review:", err);

            // Extract error message from backend response
            let errorMessage = "Failed to submit review. Please try again.";

            // Get status code (handle both custom apiClient and axios structures)
            const status = err.status || err.response?.status;

            // Get error data/message
            const errorData = err.response?.data || err;

            // Check for 403 Forbidden (Authorization error)
            if (status === 403) {
                errorMessage = "⛔ Access Denied: You are not authorized to review this task. Only the task owner can submit a review.";
                setIsForbidden(true);
            }
            // Check for 400 Bad Request (Validation errors)
            else if (status === 400) {
                if (typeof errorData === 'string') {
                    errorMessage = errorData;
                } else if (errorData.message) {
                    errorMessage = errorData.message;
                } else if (errorData.error) {
                    errorMessage = errorData.error;
                }
            }
            // General error message extraction
            else if (errorData) {
                if (typeof errorData === 'string') {
                    errorMessage = errorData;
                } else if (errorData.message) {
                    errorMessage = errorData.message;
                } else if (errorData.error) {
                    errorMessage = errorData.error;
                }
            }
            // Fallback to error object message
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
                <h1 className="section-heading">{reviewId ? "Edit Review" : "Write a Review"}</h1>

                {isLoading ? (
                    <div style={{ textAlign: "center", padding: "2rem" }}>Loading...</div>
                ) : (
                    <>
                        {error && <div className="alert alert-error">{error}</div>}

                        {!isForbidden && (
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
                        )}
                    </>
                )}
            </div>
        </main>
    );
}

export default SubmitReviewPage;

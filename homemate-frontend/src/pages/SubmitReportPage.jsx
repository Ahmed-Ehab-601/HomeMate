import React, { useState } from "react";
import { useParams } from "react-router-dom";
import { apiFetch, baseUrl } from "../utils/apiClient";
import "../styles/ReportForm.css";

export default function SubmitReportPage() {
  const { taskId } = useParams();
  const [formData, setFormData] = useState({
    header: "",
    body: "",
  });
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitted(false);
    setError("");
    setLoading(true);

    // Frontend validation for length constraints
    if (formData.header.trim().length > 100) {
      setError("Header must be 100 characters or less.");
      return;
    }
    if (formData.body.trim().length > 500) {
      setError("Body must be 500 characters or less.");
      return;
    }

    try {
      const response = await apiFetch(`${baseUrl}/api/reports`, {
        method: "POST",
        body: JSON.stringify({
          taskID: Number(taskId),
          header: formData.header,
          body: formData.body,
        }),
      });

      if (!response.ok) {
        if (response.status === 403) {
          throw new Error("you aren't allowed to make reports Mr.admin");
        }
        const text = await response.text();
        throw new Error(text || "Failed to submit report");
      }

      setSubmitted(true);
      setFormData({ header: "", body: "" });
    } catch (err) {
      setError(err.message || "Failed to submit report");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="submit-report-page">
      <div className="submit-report-page__container">
        <div className="submit-report-page__form-wrapper">
          <h1 className="submit-report-page__title">Report Form</h1>
          <p className="submit-report-page__subtitle">Task #{taskId}</p>

          {submitted && (
            <div className="submit-report-page__success-message">
              Report submitted successfully!
            </div>
          )}

          {error && (
            <div className="submit-report-page__success-message" style={{ background: "#f44336" }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="submit-report-page__form-group">
              <label htmlFor="header" className="submit-report-page__label">
                Header
              </label>
              <input
                type="text"
                id="header"
                name="header"
                value={formData.header}
                onChange={handleChange}
                placeholder="Enter report header"
                className="submit-report-page__input"
                maxLength={100}
                required
              />
            </div>

            <div className="submit-report-page__form-group">
              <label htmlFor="body" className="submit-report-page__label">
                Body
              </label>
              <textarea
                id="body"
                name="body"
                value={formData.body}
                onChange={handleChange}
                placeholder="Enter report content"
                rows="6"
                className="submit-report-page__textarea"
                maxLength={500}
                required
              />
            </div>

            <button
              type="submit"
              className="submit-report-page__submit-btn"
              disabled={loading}
            >
              {loading ? "Submitting..." : "Submit Report"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
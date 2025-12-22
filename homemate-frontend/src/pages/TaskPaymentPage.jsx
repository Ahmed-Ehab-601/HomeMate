import React, { useState, useEffect } from "react";
import { loadStripe } from "@stripe/stripe-js";
import { Elements, CardElement, CardNumberElement, CardExpiryElement, CardCvcElement, useStripe, useElements } from "@stripe/react-stripe-js";
import { useSearchParams, useNavigate } from "react-router-dom";
import { baseUrl, apiRequest } from "../utils/apiClient";
import "../styles/TaskDetails.css";

const publishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;
const stripePromise = loadStripe(publishableKey);

function PaymentForm({ taskId, userId, taskerId, billAmount, onSuccess }) {
  const stripe = useStripe();
  const elements = useElements();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handlePayment = async (e) => {
    e.preventDefault();
    if (!stripe || !elements) return;
    setLoading(true);
    setError(null);

    try {
      // STEP 1: create payment on backend
      const createPayload = { taskId: Number(taskId), userId: Number(userId), taskerId: Number(taskerId), bill: Number(billAmount) };
        const createResp = await apiRequest(`${baseUrl}/api/tasker/payments/create`, {
        method: "POST",
        body: JSON.stringify(createPayload),
      });
        console.log("/api/tasker/payments/create response:", createResp);

      // backend may return { clientSecret } or { clientSecret, paymentId }
      const clientSecret = createResp?.clientSecret || createResp?.client_secret || createResp;
      const paymentId = createResp?.paymentId || createResp?.payment_id || null;

      if (!clientSecret) throw new Error("Missing client secret from server");

      // STEP 2: confirm card payment with Stripe
          const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
            payment_method: {
              card: elements.getElement(CardNumberElement) || elements.getElement(CardElement),
            },
          });

      if (stripeError) {
        setError(stripeError.message || "Payment failed");
        return;
      }

      if (paymentIntent && paymentIntent.status === "succeeded") {
        // STEP 3: notify backend to verify and mark payment as paid
        await apiRequest(`${baseUrl}/api/tasker/payments/confirm`, {
          method: "POST",
          body: JSON.stringify({ paymentIntentId: paymentIntent.id, paymentId, status: "succeeded" }),
        });

        if (onSuccess) onSuccess(paymentIntent);
      } else {
        setError("Payment did not complete successfully");
      }
    } catch (err) {
      console.error("Payment error:", err);
      setError(err.message || "Payment failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handlePayment} className="payment-form">
      <h2>Pay for Task</h2>
      <p>Total Amount: <strong>${Number(billAmount).toFixed(2)}</strong></p>
      <div className="card-field">
        <label className="card-label">Credit Card</label>
        <div className="card-number-box">
          <CardNumberElement options={{ style: { base: { fontSize: '16px', color: '#111827' } } }} />
        </div>

        <div className="card-row">
          <div className="card-small-box">
            <label className="small-label">Expiry</label>
            <div className="small-input"><CardExpiryElement options={{ style: { base: { fontSize: '14px', color: '#111827' } } }} /></div>
          </div>
          <div className="card-small-box">
            <label className="small-label">CVV/CVC</label>
            <div className="small-input"><CardCvcElement options={{ style: { base: { fontSize: '14px', color: '#111827' } } }} /></div>
          </div>
        </div>
      </div>

      {error && <div className="payment-error">{error}</div>}

      <button type="submit" className="btn btn-primary payment-button" disabled={!stripe || loading}>
        {loading ? "Processing..." : `Pay $${Number(billAmount).toFixed(2)}`}
      </button>

      <div style={{ marginTop: 12, color: "#6b7280" }}>
        <div><strong>Test card:</strong> 4242 4242 4242 4242 — any future expiry / CVC</div>
      </div>
    </form>
  );
}

export default function TaskPaymentPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const taskId = searchParams.get("taskId");
  const userId = searchParams.get("userId");
  const taskerId = searchParams.get("taskerId");
  const billAmount = searchParams.get("bill") || searchParams.get("amount");

  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (!publishableKey) {
      console.warn("VITE_STRIPE_PUBLISHABLE_KEY is not set. Payment may not work in this environment.");
    }
  }, []);

  if (!taskId || !userId || !taskerId || !billAmount) {
    return (
      <main className="page page--signup">
        <div className="signin-container">
          <div className="signin-card" style={{ maxWidth: 540 }}>
            <h2 className="signin-title">Missing payment information</h2>
            <p>Please open this page from the task details 'Pay Online' button.</p>
          </div>
        </div>
      </main>
    );
  }

  if (success) {
    return (
      <main className="page page--signup">
        <div className="signin-container">
          <div className="signin-card" style={{ maxWidth: 540 }}>
            <div className="success-card">
              <h1 className="signin-title">✅ Payment Successful</h1>
              <p>Amount: ${Number(billAmount).toFixed(2)}</p>
              <p>Redirecting to your tasks...</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="page page--signup">
      <div className="signin-container">
        <div className="signin-card" style={{ maxWidth: 540 }}>
          <h1 className="signin-title">Complete Payment</h1>
          <p className="signin-subtitle">Task #{taskId} — Pay securely with card</p>
          <Elements stripe={stripePromise}>
            <div className="signin-form">
              <PaymentForm
                taskId={taskId}
                userId={userId}
                taskerId={taskerId}
                billAmount={billAmount}
                onSuccess={(pi) => {
                  setSuccess(true);
                  setTimeout(() => navigate(`/tasks/${taskId}`), 2000);
                }}
              />
            </div>
          </Elements>
        </div>
      </div>
    </main>
  );
}

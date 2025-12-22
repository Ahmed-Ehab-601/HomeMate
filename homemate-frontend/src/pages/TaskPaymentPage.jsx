import React, { useState, useEffect } from "react";
import { loadStripe } from "@stripe/stripe-js";
import { Elements, CardElement, useStripe, useElements } from "@stripe/react-stripe-js";
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
          card: elements.getElement(CardElement),
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
      <div className="card-element-wrapper">
        <CardElement
          options={{
            style: {
              base: { fontSize: "16px", color: "#424770", "::placeholder": { color: "#aab7c4" } },
              invalid: { color: "#9e2146" },
            },
          }}
        />
      </div>

      {error && <div className="payment-error">{error}</div>}

      <button type="submit" className="btn btn-primary" disabled={!stripe || loading}>
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
      <div style={{ padding: 24 }}>
        <h2>Missing payment information</h2>
        <p>Please open this page from the task details 'Pay Online' button.</p>
      </div>
    );
  }

  if (success) {
    return (
      <div style={{ padding: 24 }}>
        <div className="success-card">
          <h1>✅ Payment Successful</h1>
          <p>Amount: ${Number(billAmount).toFixed(2)}</p>
          <p>Redirecting to your tasks...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="task-payment-page">
      <div className="task-payment-card">
        <h1>Complete Payment</h1>
        <Elements stripe={stripePromise}>
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
        </Elements>
      </div>
    </div>
  );
}

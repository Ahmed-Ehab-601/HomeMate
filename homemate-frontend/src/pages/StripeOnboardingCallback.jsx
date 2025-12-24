import { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";

export default function StripeOnboardingCallback() {
  const navigate = useNavigate();
  const { search } = useLocation();
  const params = new URLSearchParams(search);

  useEffect(() => {
    // Optional: you can inspect params for messages from backend/stripe
    // e.g. ?success=true&account=acct_...
    // After short delay, return user to tasker dashboard so UI can refresh
    const timer = setTimeout(() => {
      // Navigate back to tasker dashboard where profile will be refreshed
      navigate("/tasker/profile");
    }, 1500);

    return () => clearTimeout(timer);
  }, [navigate, search]);

  const account = params.get("account") || params.get("acct") || params.get("stripe_account") || null;
  const success = params.get("success") || params.get("status") || null;

  return (
    <main className="page page--wide">
      <div className="profile-shell">
        <header>
          <h1 className="section-heading">Stripe Onboarding</h1>
          <p className="tasker-card__meta">You will be returned to your dashboard shortly.</p>
        </header>

        <section className="card profile-panel">
          <div style={{ padding: 8 }}>
            {success ? (
              <>
                <p style={{ fontWeight: 700 }}>Onboarding completed</p>
                <p>Thank you — you will be redirected to your Tasker Hub.</p>
              </>
            ) : (
              <>
                <p style={{ fontWeight: 700 }}>Onboarding finished</p>
                <p>If your account was successfully connected, you will be redirected shortly.</p>
              </>
            )}

            {account && (
              <p style={{ marginTop: 12 }}>
                Account: <code>{account}</code>
              </p>
            )}

            <div className="form-actions" style={{ marginTop: 16 }}>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => navigate("/tasker/profile")}
              >
                Go to Tasker Hub now
              </button>
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}

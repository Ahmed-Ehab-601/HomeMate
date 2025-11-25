import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import Modal from "../components/Modal";
import {
  addUserAddress,
  deleteUserAccount,
  deleteUserAddress,
  getUserAddresses,
  getUserProfile,
  updateUserAddress,
} from "../api/userProfileApi";

const emptyAddress = {
  country: "",
  city: "",
  street: "",
  apartment: "",
};

const normalizeAddress = (address) => {
  if (!address) return null;
  return {
    addressId: address.addressId ?? address.id ?? address.addressID ?? address.address_id ?? null,
    userId: address.userId ?? address.userID ?? address.user_id ?? null,
    country: address.country ?? "",
    city: address.city ?? "",
    street: address.street ?? "",
    apartment: address.apartment ?? "",
  };
};

function UserProfilePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [profileStatus, setProfileStatus] = useState("loading");
  const [profileError, setProfileError] = useState(null);

  const [addresses, setAddresses] = useState([]);
  const [addressesStatus, setAddressesStatus] = useState("loading");
  const [addressesError, setAddressesError] = useState(null);

  const [addForm, setAddForm] = useState(emptyAddress);
  const [editDraft, setEditDraft] = useState(null);
  const [formErrors, setFormErrors] = useState({});
  const [isSavingAddress, setSavingAddress] = useState(false);

  const [feedback, setFeedback] = useState(null);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [isDeletingAccount, setDeletingAccount] = useState(false);

  useEffect(() => {
    if (!user) {
      navigate("/");
      return;
    }
    loadProfile();
    loadAddresses();
  }, [user, navigate]);

  const loadProfile = () => {
    setProfileStatus("loading");
    setProfileError(null);
    getUserProfile()
      .then((data) => {
        setProfile(data);
        setProfileStatus("success");
      })
      .catch((error) => {
        setProfileStatus("error");
        setProfileError(error);
      });
  };

  const loadAddresses = () => {
    setAddressesStatus("loading");
    setAddressesError(null);
    getUserAddresses()
      .then((data) => {
        setAddresses((data ?? []).map(normalizeAddress).filter(Boolean));
        setAddressesStatus("success");
      })
      .catch((error) => {
        setAddressesStatus("error");
        setAddressesError(error);
      });
  };

  const fullName = useMemo(() => {
    if (!profile) return "";
    const parts = [profile.firstName, profile.lastName].filter(Boolean);
    return parts.length ? parts.join(" ") : profile.username;
  }, [profile]);

  const formatDate = (value) => {
    if (!value) return "—";
    try {
      return new Date(value).toLocaleDateString();
    } catch (error) {
      return value;
    }
  };

  const dismissFeedback = () => setFeedback(null);

  const validateAddress = (values) => {
    const errors = {};
    if (!values.country?.trim()) errors.country = "Country is required.";
    if (!values.city?.trim()) errors.city = "City is required.";
    if (!values.street?.trim()) errors.street = "Street is required.";
    return errors;
  };

  const handleAddAddressSubmit = (event) => {
    event.preventDefault();
    if (!user) return;
    const errors = validateAddress(addForm);
    setFormErrors(errors);
    if (Object.keys(errors).length) return;
    setSavingAddress(true);
    addUserAddress(addForm)
      .then(() => {
        setAddForm(emptyAddress);
        setFeedback({ type: "success", message: "Address added successfully." });
        loadAddresses();
      })
      .catch((error) => {
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to add address. Please try again.",
        });
      })
      .finally(() => setSavingAddress(false));
  };

  const startEditing = (address) => {
    setEditDraft(address ? { ...address } : null);
    setFormErrors({});
  };

  const handleEditChange = (field, value) => {
    setEditDraft((prev) => (prev ? { ...prev, [field]: value } : prev));
  };

  const handleUpdateAddressSubmit = (event) => {
    event.preventDefault();
    if (!editDraft || !user) return;
    const errors = validateAddress(editDraft);
    setFormErrors(errors);
    if (Object.keys(errors).length) return;
    setSavingAddress(true);
    updateUserAddress(editDraft.addressId, {
      ...editDraft,
    })
      .then(() => {
        setFeedback({ type: "success", message: "Address updated." });
        setEditDraft(null);
        loadAddresses();
      })
      .catch((error) => {
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update address. Please try again.",
        });
      })
      .finally(() => setSavingAddress(false));
  };

  const handleDeleteAddress = (addressId) => {
    if (!addressId || !user) return;
    const confirmed = window.confirm("Remove this address from your profile?");
    if (!confirmed) return;
    deleteUserAddress(addressId)
      .then(() => {
        setFeedback({ type: "success", message: "Address removed." });
        loadAddresses();
      })
      .catch((error) => {
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to remove address. Please try again.",
        });
      });
  };

  const handleDeleteAccount = () => {
    if (!user) return;
    setDeletingAccount(true);
    deleteUserAccount()
      .then(() => {
        setFeedback({
          type: "success",
          message: "Your HomeMate account has been deleted.",
        });
        setProfile(null);
        setProfileStatus("deleted");
        setAddresses([]);
        setAddressesStatus("success");
        setDeleteModalOpen(false);
      })
      .catch((error) => {
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to delete account. Please try again.",
        });
      })
      .finally(() => setDeletingAccount(false));
  };

  const renderAddressesSection = () => {
    if (addressesStatus === "loading") {
      return <p className="tasker-card__meta">Loading your saved addresses…</p>;
    }

    if (addressesStatus === "error") {
      return (
        <div className="alert alert-error">
          <div>
            We couldn’t load your addresses.{" "}
            {addressesError?.message ?? "Please check your connection and retry."}
          </div>
          <div className="form-actions" style={{ marginTop: "8px", justifyContent: "flex-start" }}>
            <button type="button" className="btn btn-primary" onClick={loadAddresses}>
              Retry
            </button>
          </div>
        </div>
      );
    }

    if (!addresses.length) {
      return (
        <div className="empty-state">
          <p>No addresses yet.</p>
          <p>Add your first address using the form on the right.</p>
        </div>
      );
    }

    return (
      <ul className="address-list">
        {addresses.map((address) => (
          <li key={address.addressId ?? address.street} className="address-item">
            <div className="address-item__body">
              <p className="address-item__title">
                {address.street}
                {address.apartment ? `, ${address.apartment}` : ""}
              </p>
              <p className="address-item__meta">
                {address.city}, {address.country}
              </p>
            </div>
            <div className="address-item__actions">
              <button type="button" className="btn btn-ghost" onClick={() => startEditing(address)}>
                Edit
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => handleDeleteAddress(address.addressId)}
              >
                Remove
              </button>
            </div>
          </li>
        ))}
      </ul>
    );
  };

  return (
    <main className="page page--wide">
      <div className="profile-shell">
        <header>
          <p className="section-kicker">Your account</p>
          <h1 className="section-heading">HomeMate profile</h1>
          <p className="tasker-card__meta">
            Keep your personal information and service addresses up to date.
          </p>
        </header>

        {feedback && (
          <div
            className={`alert-banner ${
              feedback.type === "error" ? "error" : "success"
            } profile-alert`}
          >
            <span>{feedback.message}</span>
            <button type="button" className="alert-dismiss" onClick={dismissFeedback}>
              ×
            </button>
          </div>
        )}

        <section className="card profile-panel">
          {profileStatus === "loading" && <p className="tasker-card__meta">Loading profile…</p>}
          {profileStatus === "error" && (
            <div className="alert alert-error">
              <div>
                We couldn’t load your profile.{" "}
                {profileError?.message ?? "Please refresh and try again."}
              </div>
              <div className="form-actions" style={{ marginTop: "8px" }}>
                <button type="button" className="btn btn-primary" onClick={loadProfile}>
                  Retry
                </button>
              </div>
            </div>
          )}
          {profileStatus === "deleted" && (
            <div className="alert alert-warning">
              You deleted this account. Please sign out or contact support if that was a mistake.
            </div>
          )}
          {profileStatus === "success" && profile && (
            <div className="profile-summary">
              <div>
                <p className="section-kicker">Profile overview</p>
                <h2 className="section-heading" style={{ marginBottom: "8px" }}>
                  {fullName}
                </h2>
                <p className="tasker-card__meta">Username: {profile.username}</p>
              </div>
              <dl className="profile-summary__grid">
                <div>
                  <dt>Email</dt>
                  <dd>{profile.email ?? "—"}</dd>
                </div>
                <div>
                  <dt>Phone</dt>
                  <dd>{profile.phone ?? "—"}</dd>
                </div>
                <div>
                  <dt>Date of birth</dt>
                  <dd>{formatDate(profile.birthDate)}</dd>
                </div>
                <div>
                  <dt>Gender</dt>
                  <dd>{profile.gender ?? "—"}</dd>
                </div>
                <div>
                  <dt>Status</dt>
                  <dd>{profile.suspended ? "Suspended" : "Active"}</dd>
                </div>
                <div>
                  <dt>Role</dt>
                  <dd>{profile.admin ? "Administrator" : "Customer"}</dd>
                </div>
              </dl>
            </div>
          )}
        </section>

        <section className="profile-grid">
          <article className="card profile-panel">
            <div className="section-title" style={{ alignItems: "center" }}>
              <div>
                <p className="section-kicker">Saved addresses</p>
                <h2 className="section-heading">Service locations</h2>
              </div>
              <button type="button" className="btn btn-ghost" onClick={loadAddresses}>
                Refresh
              </button>
            </div>
            {renderAddressesSection()}
          </article>

          <article className="card profile-panel">
            <p className="section-kicker">{editDraft ? "Edit address" : "Add address"}</p>
            <h2 className="section-heading" style={{ marginBottom: "8px" }}>
              {editDraft ? "Update address details" : "Add a new service address"}
            </h2>
            <form
              className="profile-form"
              onSubmit={editDraft ? handleUpdateAddressSubmit : handleAddAddressSubmit}
            >
              <div className="form-field">
                <label htmlFor="country-input">Country*</label>
                <input
                  id="country-input"
                  className={`input ${
                    formErrors.country ? "error" : ""
                  }`}
                  value={editDraft ? editDraft.country : addForm.country}
                  onChange={(event) =>
                    editDraft
                      ? handleEditChange("country", event.target.value)
                      : setAddForm((prev) => ({ ...prev, country: event.target.value }))
                  }
                />
                {formErrors.country && <p className="error-text">{formErrors.country}</p>}
              </div>
              <div className="form-field">
                <label htmlFor="city-input">City*</label>
                <input
                  id="city-input"
                  className={`input ${formErrors.city ? "error" : ""}`}
                  value={editDraft ? editDraft.city : addForm.city}
                  onChange={(event) =>
                    editDraft
                      ? handleEditChange("city", event.target.value)
                      : setAddForm((prev) => ({ ...prev, city: event.target.value }))
                  }
                />
                {formErrors.city && <p className="error-text">{formErrors.city}</p>}
              </div>
              <div className="form-field">
                <label htmlFor="street-input">Street*</label>
                <input
                  id="street-input"
                  className={`input ${formErrors.street ? "error" : ""}`}
                  value={editDraft ? editDraft.street : addForm.street}
                  onChange={(event) =>
                    editDraft
                      ? handleEditChange("street", event.target.value)
                      : setAddForm((prev) => ({ ...prev, street: event.target.value }))
                  }
                />
                {formErrors.street && <p className="error-text">{formErrors.street}</p>}
              </div>
              <div className="form-field">
                <label htmlFor="apartment-input">Apartment / Unit</label>
                <input
                  id="apartment-input"
                  className="input"
                  value={editDraft ? editDraft.apartment : addForm.apartment}
                  onChange={(event) =>
                    editDraft
                      ? handleEditChange("apartment", event.target.value)
                      : setAddForm((prev) => ({ ...prev, apartment: event.target.value }))
                  }
                />
              </div>
              <div className="form-actions">
                {editDraft && (
                  <button
                    type="button"
                    className="btn btn-ghost"
                    onClick={() => {
                      setEditDraft(null);
                      setFormErrors({});
                    }}
                  >
                    Cancel
                  </button>
                )}
                <button type="submit" className="btn btn-primary" disabled={isSavingAddress}>
                  {isSavingAddress ? "Saving…" : editDraft ? "Update address" : "Add address"}
                </button>
              </div>
            </form>
          </article>
        </section>

        <section className="card danger-zone">
          <div>
            <p className="section-kicker">Danger zone</p>
            <h2 className="section-heading">Delete account</h2>
            <p className="tasker-card__meta">
              Permanently remove your profile and all saved data. This action cannot be undone.
            </p>
          </div>
          <button
            type="button"
            className="btn btn-danger"
            onClick={() => setDeleteModalOpen(true)}
          >
            Delete account
          </button>
        </section>
      </div>

      {isDeleteModalOpen && (
        <Modal
          title="Delete account"
          icon="⚠️"
          onClose={() => setDeleteModalOpen(false)}
          actions={
            <>
              <button type="button" className="btn btn-ghost" onClick={() => setDeleteModalOpen(false)}>
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={handleDeleteAccount}
                disabled={isDeletingAccount}
              >
                {isDeletingAccount ? "Deleting…" : "Delete"}
              </button>
            </>
          }
        >
          <p>
            Are you sure you want to delete your HomeMate account? This removes your profile, saved
            addresses, and any upcoming tasks.
          </p>
        </Modal>
      )}
    </main>
  );
}

export default UserProfilePage;


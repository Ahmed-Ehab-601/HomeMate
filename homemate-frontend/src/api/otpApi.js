// OTP API for email verification and password reset
import { baseUrl } from "../utils/apiClient";

/**
 * Send OTP to user's email
 * @param {string} email - User's email address
 * @param {string} emailType - Type of email: 'EMAIL_VERIFICATION' or 'FORGOT_PASSWORD'
 * @returns {Promise<{message: string}>}
 */
export async function sendOtp(email, emailType) {
    const response = await fetch(`${baseUrl}/api/auth/otp/send`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            recipientEmail: email,
            emailType: emailType,
        }),
    });

    if (!response.ok) {
        const error = await response.json().catch(() => ({
            message: "Failed to send OTP"
        }));
        throw new Error(error.message || "Failed to send OTP");
    }

    return response.json();
}

/**
 * Verify OTP code
 * @param {string} email - User's email address
 * @param {string} code - OTP code
 * @param {string} emailType - Type of email: 'EMAIL_VERIFICATION' or 'FORGOT_PASSWORD'
 * @returns {Promise<{success: boolean, message: string, token: string}>}
 */
export async function verifyOtp(email, code, emailType) {
    const response = await fetch(`${baseUrl}/api/auth/otp/verify`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            recipientEmail: email,
            code: code,
            emailType: emailType,
        }),
    });

    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.message || "Invalid OTP code");
    }

    return result;
}

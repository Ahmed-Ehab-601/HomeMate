// OTP API for email verification and password reset
import { baseUrl } from "../utils/apiClient";

/**
 * Send OTP to user's email
 * @param {string} email - User's email address
 * @param {string} emailType - Type of email: 'EMAIL_VERIFICATION' or 'FORGOT_PASSWORD'
 * @param {string} type - OTP request type: 'signup' or 'forgetpassword'
 * @returns {Promise<{success: boolean, message: string}>}
 */
export async function sendOtp(email, emailType, type = 'signup') {
    // Determine the type parameter based on emailType if not explicitly provided
    let otpType = type;
    if (emailType === 'FORGOT_PASSWORD') {
        otpType = 'forgetpassword';
    } else if (emailType === 'EMAIL_VERIFICATION') {
        otpType = 'signup';
    }

    const response = await fetch(`${baseUrl}/api/auth/otp/send/${otpType}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            recipientEmail: email,
            emailType: emailType,
        }),
    });

    const result = await response.json();

    if (!response.ok) {
        return {
            success: false,
            message: result.message || "Failed to send OTP"
        };
    }

    return {
        success: result.success !== undefined ? result.success : true,
        message: result.message || "OTP sent successfully"
    };
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

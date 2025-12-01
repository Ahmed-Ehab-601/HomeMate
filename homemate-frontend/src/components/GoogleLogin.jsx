import { GoogleLogin as GoogleOAuthLogin } from "@react-oauth/google";
import { loginWithGoogle } from "../api/authApi";

function GoogleLogin({ onSuccess, onError }) {

    const handleSuccess = async (credentialResponse) => {
        console.log("GoogleLogin HAPPY");
        try {
            const idToken = credentialResponse?.credential ?? "ggg";
            const response = await loginWithGoogle(idToken);
            onSuccess?.(response);
        } catch (err) {
            console.error("Google login failed", err);
            onError?.(err);
        }
    };

    const handleError = () => {
        onError?.(new Error("Google Login Failed"));
    };

    return (
        <div className="signin-google">
          <GoogleOAuthLogin
            onSuccess={handleSuccess}
            onError={handleError}
            theme="outline"
            shape="pill"
            size="large"
            width="100%"
          />
        </div>
    );
}

export default GoogleLogin;


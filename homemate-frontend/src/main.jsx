// src/main.jsx
// Polyfill `global` for libraries (sockjs-client) that expect a Node-like global
if (typeof global === "undefined") {
  // Use globalThis so this works in any modern environment
  globalThis.global = globalThis;
}

import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { GoogleOAuthProvider } from "@react-oauth/google";
import "./index.css";
import App from "./App.jsx";

const googleClientId = "953966255991-698hogtjc70cori6r61dbjqhk30dq44u.apps.googleusercontent.com";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <GoogleOAuthProvider clientId={googleClientId}>
      <App />
    </GoogleOAuthProvider>
  </StrictMode>
);
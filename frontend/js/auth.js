// Base URL for the Gateway
const API_URL = "http://localhost:8080";

const auth = {
    // 1. Save token to LocalStorage
    login: (token) => {
        localStorage.setItem("jwt_token", token);
    },

    // 2. Remove token
    logout: () => {
        localStorage.removeItem("jwt_token");
        window.location.href = "login.html";
    },

    // 3. Get the token for API calls
    getToken: () => {
        return localStorage.getItem("jwt_token");
    },

    // 4. Check if user is logged in
    isAuthenticated: () => {
        const token = localStorage.getItem("jwt_token");
        // Simple check: exists and is not empty
        return token && token.length > 10;
    },

    // 5. Helper to get headers with the token included
    getAuthHeader: () => {
        const token = localStorage.getItem("jwt_token");
        if (token) {
            return {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            };
        }
        return { "Content-Type": "application/json" };
    }
};
/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: '#4F46E5',
                background: '#F9FAFB',
                surface: '#FFFFFF',
                textPrimary: '#111827',
                textSecondary: '#6B7280',
                border: '#E5E7EB',
            },
        },
    },
    plugins: [],
}

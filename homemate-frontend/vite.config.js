import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // Also proxy direct endpoints if they don't have /api prefix
      '/getallservices': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/getservicedetails': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/create': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/edit': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/delete': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})

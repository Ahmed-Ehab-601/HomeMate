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
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // Log proxy requests for debugging
            console.log(`[Proxy] ${req.method} ${req.url} -> ${options.target}${req.url}`);

            // Handle OPTIONS preflight requests
            if (req.method === 'OPTIONS') {
              console.log(`[Proxy] Intercepting OPTIONS request for ${req.url}`);
              res.writeHead(200, {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type, Authorization',
                'Access-Control-Max-Age': '86400',
              });
              res.end();
              return;
            }

            // Add Authorization header from incoming request if not present
            const token = req.headers['authorization'];
            if (token) {
              proxyReq.setHeader('Authorization', token);
            }
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            // Add CORS headers to proxy responses
            proxyRes.headers['access-control-allow-origin'] = '*';
            proxyRes.headers['access-control-allow-methods'] = 'GET, POST, PUT, PATCH, DELETE, OPTIONS';
            proxyRes.headers['access-control-allow-headers'] = 'Content-Type, Authorization';
          });
        },
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

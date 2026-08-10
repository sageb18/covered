import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // The dev server runs on :5173 and Spring Boot on :8080. A browser will not let a
    // page from one origin call the other, so Vite forwards /api/* to the backend and the
    // browser only ever talks to :5173. In production both are served from the same jar,
    // so the same relative URLs work there with nothing to configure.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})

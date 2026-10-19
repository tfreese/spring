import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        // Diese Properties sind nur für einen laufenden vite-Server im dev-Mode.
        port: 5173,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
            // ,
            // '/public-api': {
            //     // Ziel-Host ohne Pfad; rewrite sorgt dafür, dass
            //     // /public-api/time > /public/time am Backend wird.
            //     target: 'http://localhost:8080',
            //     changeOrigin: true,
            //     rewrite: (path) => path.replace(/^\/public-api/, '/public')
            // }
        }
    },
    build: {
        outDir: 'dist',
        emptyOutDir: true,
        sourcemap: true
    }
})

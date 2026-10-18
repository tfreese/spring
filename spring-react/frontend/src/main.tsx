import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {HashRouter} from 'react-router-dom'

import App from './App'
import {applyTheme, type ThemeName} from './utils/theme'

const savedTheme: ThemeName = localStorage.getItem('theme') === 'light' ? 'light' : 'dark'
applyTheme(savedTheme)

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <HashRouter>
            <App/>
        </HashRouter>
    </StrictMode>,
)

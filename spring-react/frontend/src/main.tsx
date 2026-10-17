import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {HashRouter} from 'react-router-dom'
import App from './App'

const themeLink = document.getElementById('theme-css') as HTMLLinkElement
const savedTheme = localStorage.getItem('theme') === 'light' ? 'light' : 'dark'
themeLink.href = savedTheme === 'dark' ? '/src/styles-darcula.css' : '/src/styles-intellij-light.css'

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <HashRouter>
            <App/>
        </HashRouter>
    </StrictMode>,
)

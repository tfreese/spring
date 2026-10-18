import {useState} from 'react'
import {Outlet} from 'react-router-dom'

import type {LayoutContext} from './layoutContext'
import {applyTheme, type ThemeName} from './utils/theme'

// Gemeinsamer Rahmen: Header, Fehleranzeige und Footer bleiben stehen, <Outlet/> wird durch die aktive Kindroute ersetzt.
export default function Layout() {
    const [error, setError] = useState<string>()
    const [theme, setTheme] = useState<ThemeName>(() => localStorage.getItem('theme') === 'light' ? 'light' : 'dark')
    const context: LayoutContext = {setError}

    const toggleTheme = () => {
        const nextTheme: ThemeName = theme === 'dark' ? 'light' : 'dark'
        setTheme(nextTheme)
        applyTheme(nextTheme)
    }

    return (
        <div className="page">
            <header>
                <div style={{alignItems: 'center', display: 'flex', justifyContent: 'space-between', gap: '1rem'}}>
                    <div>
                        <p className="eyebrow">Spring Boot + React</p>
                        <h1>Task-Demo</h1>
                    </div>
                    <button
                        aria-label="Theme wechseln"
                        aria-pressed={theme === 'light'}
                        className={theme === 'light' ? 'theme-toggle is-light' : 'theme-toggle is-dark'}
                        onClick={toggleTheme}
                        type="button"
                    >
                        <span className="theme-toggle__track">
                            <span className="theme-toggle__symbol theme-toggle__symbol--sun">☀️</span>
                            <span className="theme-toggle__symbol theme-toggle__symbol--moon">🌙</span>
                            <span className="theme-toggle__thumb"/>
                        </span>
                    </button>
                </div>
            </header>

            <main>
                {error && <p className="error" role="alert">{error}</p>}

                <Outlet context={context}/>
            </main>

            <footer className="app-footer">
                <small>Spring Boot + React Demo</small>
            </footer>
        </div>
    )
}

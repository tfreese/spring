import {useState} from 'react'
import {Outlet} from 'react-router-dom'

import type {LayoutContext} from './layoutContext'

// Gemeinsamer Rahmen: Header, Fehleranzeige und Footer bleiben stehen, <Outlet/> wird durch die aktive Kindroute ersetzt.
export default function Layout() {
    const [error, setError] = useState<string>()
    const context: LayoutContext = {setError}

    return (
        <div className="page">
            <header>
                <p className="eyebrow">Spring Boot + React</p>
                <h1>Task-Demo</h1>
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


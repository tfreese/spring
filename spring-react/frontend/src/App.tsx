// Typen braucht nur der Compiler, nicht React selbst zur Laufzeit.
import type {SubmitEvent} from 'react'

// Funktionen die zur Laufzeit benötigt werden.
import {useCallback, useEffect, useState} from 'react'

type Task = {
    id: number
    title: string
}

export default function App() {
    const [tasks, setTasks] = useState<Task[]>([])
    const [title, setTitle] = useState('')
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState<string>()

    const loadTasks = useCallback(async () => {
        setLoading(true)
        setError(undefined)

        try {
            const response = await fetch('/api/tasks')
            if (!response.ok) throw new Error(`Tasks konnten nicht geladen werden: HTTP ${response.status}`)
            setTasks(await response.json() as Task[])
        } catch (reason) {
            setError(String(reason))
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => {
        void loadTasks()
    }, [loadTasks])

    async function createTask(event: SubmitEvent<HTMLFormElement>) {
        // Disable default Browser workflow (send data, reload page), this is handled by App itself.
        event.preventDefault()

        const normalizedTitle = title.trim()
        if (!normalizedTitle) return

        setSaving(true)
        setError(undefined)

        try {
            const response = await fetch('/api/tasks', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({title: normalizedTitle}),
            })

            if (!response.ok) throw new Error(`Task konnte nicht angelegt werden: HTTP ${response.status}`)
            const created = await response.json() as Task
            setTasks(current => [...current, created])
            setTitle('')
        } catch (reason) {
            setError(String(reason))
        } finally {
            setSaving(false)
        }
    }

    async function deleteTask(id: number) {
        setError(undefined)

        try {
            const response = await fetch(`/api/tasks/${id}`, {method: 'DELETE'})
            if (!response.ok) throw new Error(`Task konnte nicht gelöscht werden: HTTP ${response.status}`)
            setTasks(current => current.filter(task => task.id !== id))
        } catch (reason) {
            setError(String(reason))
        }
    }

    return (
        <main className="page">
            <header>
                <p className="eyebrow">Spring Boot + React</p>
                <h1>Task-Demo</h1>
                <p className="intro">Tasks über eine REST-API anlegen, laden und löschen.</p>
            </header>

            <form className="create-form" onSubmit={createTask}>
                <label htmlFor="task-title">Neue Aufgabe</label>
                <div className="form-row">
                    <input
                        id="task-title"
                        maxLength={200}
                        onChange={event => setTitle(event.target.value)}
                        placeholder="Was ist zu erledigen?"
                        value={title}
                    />
                    <button disabled={saving || !title.trim()} type="submit">
                        {saving ? 'Speichert …' : 'Anlegen'}
                    </button>
                </div>
            </form>

            <div className="toolbar">
                <h2>Vorhandene Tasks</h2>
                <button className="secondary" disabled={loading} onClick={() => void loadTasks()} type="button">
                    {loading ? 'Lädt …' : 'Neu laden'}
                </button>
            </div>

            {error && <p className="error" role="alert">{error}</p>}

            {!loading && tasks.length === 0 && <p className="empty">Noch keine Tasks vorhanden.</p>}

            <ul className="task-list">
                {tasks.map(task => (
                    <li key={task.id}>
                        <span>{task.title}</span>
                        <button className="danger" onClick={() => void deleteTask(task.id)} type="button">
                            Löschen
                        </button>
                    </li>
                ))}
            </ul>
        </main>
    )
}

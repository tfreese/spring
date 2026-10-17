// Typen braucht nur der Compiler, nicht React selbst zur Laufzeit.
import type {SubmitEvent} from 'react'
// Funktionen die zur Laufzeit benötigt werden.
import {useCallback, useEffect, useState} from 'react'
import {Link, Navigate, Route, Routes} from 'react-router-dom'

import Layout from './Layout'
import {useLayoutContext} from './layoutContext'
import TaskEditPage from './TaskEditPage'
import type {Task} from './models/task'

export default function App() {
    return (
        <Routes>
            <Route element={<Layout/>}>
                <Route element={<TaskListPage/>} path="/"/>
                <Route element={<TaskEditPage/>} path="/tasks/:id/edit"/>
            </Route>
            <Route element={<Navigate replace to="/"/>} path="*"/>
        </Routes>
    )
}

function TaskListPage() {
    const [tasks, setTasks] = useState<Task[]>([])
    const [title, setTitle] = useState('')
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const {setError} = useLayoutContext()

    // loadTasks accepts an optional AbortSignal so we can cancel it when the
    // component is unmounted (StrictMode in development mounts/unmounts twice).
    const loadTasks = useCallback(async (signal?: AbortSignal) => {
        setLoading(true)
        setError(undefined)

        try {
            const response = await fetch('/api/tasks', {signal})
            if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
            // Only set state if the call wasn't aborted
            if (!signal?.aborted) setTasks(await response.json() as Task[])
        } catch (reason) {
            if (signal?.aborted) return
            setError(String(reason))
        } finally {
            if (!signal?.aborted) setLoading(false)
        }
    }, [setError])

    useEffect(() => {
        const controller = new AbortController()
        void loadTasks(controller.signal)
        return () => controller.abort()
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

            if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
            const created = await response.json() as Task
            setTasks(current => [...current, created])
            setTitle('')
        } catch (reason) {
            setError(String(reason))
        } finally {
            setSaving(false)
        }
    }

    function confirmDeleteTask(task: Task) {
        const confirmed = window.confirm(`Task "${task.title}" wirklich löschen?`)
        if (confirmed) {
            void deleteTask(task.id)
        }
    }

    async function deleteTask(id: number) {
        setError(undefined)

        try {
            const response = await fetch(`/api/tasks/${id}`, {method: 'DELETE'})
            if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
            setTasks(current => current.filter(task => task.id !== id))
        } catch (reason) {
            setError(String(reason))
        }
    }

    function formatCreatedAt(value: string | null) {
        if (!value) return '—'

        try {
            return new Intl.DateTimeFormat('de-DE', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
            }).format(new Date(value))
        } catch {
            return value
        }
    }

    return (
        <>
            <h2>Tasks über eine REST-API anlegen, laden und löschen.</h2>

            <form className="create-form" onSubmit={createTask}>
                <label htmlFor="task-title">Neue Aufgabe</label>
                <div className="form-row">
                    <input
                        id="task-title"
                        maxLength={31}
                        onChange={event => setTitle(event.target.value)}
                        placeholder="Was ist zu erledigen?"
                        value={title}
                    />
                    <button disabled={saving || !title.trim()} type="submit">
                        {saving ? 'Speichert ...' : 'Anlegen'}
                    </button>
                </div>
            </form>

            <div className="toolbar">
                <h2>Vorhandene Tasks</h2>
                <button className="secondary" disabled={loading} onClick={() => void loadTasks()} type="button">
                    {loading ? 'Lädt ...' : 'Neu laden'}
                </button>
            </div>

            {!loading && tasks.length === 0 && <p className="empty">Noch keine Tasks vorhanden.</p>}

            {!loading && tasks.length > 0 && (
                <table className="task-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Titel</th>
                        <th>Erstellt</th>
                        <th>Aktionen</th>
                    </tr>
                    </thead>
                    <tbody>
                    {tasks.map(task => (
                        <tr key={task.id}>
                            <td>{task.id}</td>
                            <td>{task.title}</td>
                            <td>{formatCreatedAt(task.createdAt)}</td>
                            <td>
                                <div className="actions">
                                    <Link className="button secondary" to={`/tasks/${task.id}/edit`}>
                                        Bearbeiten
                                    </Link>
                                    <button className="danger" onClick={() => confirmDeleteTask(task)} type="button">
                                        Löschen
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </>
    )
}

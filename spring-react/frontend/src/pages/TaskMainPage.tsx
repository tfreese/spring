import type {SubmitEvent} from 'react'
import {useCallback, useEffect, useMemo, useState} from 'react'
import {Link} from 'react-router-dom'

import TaskChart from '../components/TaskChart'
import {useLayoutContext} from '../layoutContext'
import type {Task} from '../models/task'
import {formatDateTime} from '../utils/date'

export default function TaskMainPage() {
    const [tasks, setTasks] = useState<Task[]>([])
    const [title, setTitle] = useState('')
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const {setError} = useLayoutContext()

    const chartData = useMemo(() => {
        const counts = new Map<string, number>()

        for (const task of tasks) {
            if (!task.createdAt) continue

            const date = new Date(task.createdAt)
            if (Number.isNaN(date.getTime())) continue

            const label = new Intl.DateTimeFormat('de-DE', {
                day: '2-digit',
                month: '2-digit',
            }).format(date)

            counts.set(label, (counts.get(label) ?? 0) + 1)
        }

        return Array.from(counts.entries()).map(([label, value]) => ({label, value}))
    }, [tasks])

    const maxChartValue = Math.max(1, ...chartData.map(item => item.value))

    const loadTasks = useCallback((signal?: AbortSignal) => {
        setLoading(true)
        setError(undefined)

        const activeSignal = signal ?? new AbortController().signal

        fetch('/api/tasks', {signal: activeSignal})
            .then(async response => {
                if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
                if (activeSignal.aborted) return undefined
                return await response.json() as Task[]
            })
            .then(tasks => {
                if (activeSignal.aborted || tasks === undefined) return
                setTasks(tasks)
            })
            .catch(reason => {
                if (activeSignal.aborted) return
                setError(String(reason))
            })
            .finally(() => {
                if (!activeSignal.aborted) setLoading(false)
            })
    }, [setError])

    useEffect(() => {
        const controller = new AbortController()
        loadTasks(controller.signal)
        return () => controller.abort()
    }, [loadTasks])

    function createTask(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()

        const normalizedTitle = title.trim()
        if (!normalizedTitle) return

        setSaving(true)
        setError(undefined)

        const controller = new AbortController()

        fetch('/api/tasks', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({title: normalizedTitle}),
            signal: controller.signal,
        })
            .then(async response => {
                if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
                if (controller.signal.aborted) return undefined
                return await response.json() as Task
            })
            .then(created => {
                if (controller.signal.aborted || created === undefined) return
                setTasks(current => [...current, created])
                setTitle('')
            })
            .catch(reason => {
                if (controller.signal.aborted) return
                setError(String(reason))
            })
            .finally(() => {
                if (!controller.signal.aborted) setSaving(false)
            })
    }

    function confirmDeleteTask(task: Task) {
        const confirmed = window.confirm(`Task "${task.title}" wirklich löschen?`)

        if (confirmed) {
            deleteTask(task.id)
        }
    }

    function deleteTask(id: number) {
        setError(undefined)

        const controller = new AbortController()

        fetch(`/api/tasks/${id}`, {method: 'DELETE', signal: controller.signal})
            .then(async response => {
                if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
                return !controller.signal.aborted
            })
            .then(success => {
                if (controller.signal.aborted || !success) return
                setTasks(current => current.filter(task => task.id !== id))
            })
            .catch(reason => {
                if (controller.signal.aborted) return
                setError(String(reason))
            })
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
                <button className="secondary" disabled={loading} onClick={() => loadTasks()} type="button">
                    {loading ? 'Lädt ...' : 'Neu laden'}
                </button>
            </div>

            {!loading && tasks.length === 0 && <p className="empty">Noch keine Tasks vorhanden.</p>}

            {!loading && tasks.length > 0 && (
                <>
                    <table>
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
                                <td>{formatDateTime(task.createdAt)}</td>
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

                    <TaskChart chartData={chartData} maxChartValue={maxChartValue}/>
                </>
            )}
        </>
    )
}

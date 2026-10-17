import type {SubmitEvent} from 'react'
import {useEffect, useState} from 'react'
import {Link, useNavigate, useParams} from 'react-router-dom'

import {useLayoutContext} from './layoutContext'
import type {Task} from './models/task'

export default function TaskEditPage() {
    // Wert des Pfad-Parameters ":id" aus der Route "/tasks/:id/edit".
    const {id} = useParams<{ id: string }>()

    const [title, setTitle] = useState('')
    const [createdAt, setCreatedAt] = useState('')
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const {setError} = useLayoutContext()
    const navigate = useNavigate()

    useEffect(() => {
        const controller = new AbortController()

        async function load() {
            setLoading(true)
            setError(undefined)

            try {
                const response = await fetch(`/api/tasks/${id}`, {signal: controller.signal})
                if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
                const task = await response.json() as Task
                setTitle(task.title)
                setCreatedAt(task.createdAt)
            } catch (reason) {
                if (!controller.signal.aborted) setError(String(reason))
            } finally {
                if (!controller.signal.aborted) setLoading(false)
            }
        }

        void load()
        return () => controller.abort()
    }, [id, setError])

    async function save(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()

        const normalizedTitle = title.trim()
        const normalizedCreatedAt = createdAt.trim()
        if (!normalizedTitle || !normalizedCreatedAt) return

        setSaving(true)
        setError(undefined)

        try {
            const response = await fetch(`/api/tasks/${id}`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({title: normalizedTitle, createdAt: normalizedCreatedAt}),
            })
            if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
            // Nach dem erfolgreichen Speichern zur Liste navigieren.
            void navigate('/')
        } catch (reason) {
            setError(String(reason))
        } finally {
            setSaving(false)
        }
    }

    return (
        <>
            <Link className="button secondary" to="/">
                ← Zurück
            </Link>

            <h2>Task bearbeiten</h2>

            <form className="create-form" onSubmit={save}>
                <div className="form-field">
                    <label htmlFor="edit-title">Titel (Task {id})</label>
                    <input
                        disabled={loading}
                        id="edit-title"
                        maxLength={31}
                        onChange={event => setTitle(event.target.value)}
                        onKeyDown={event => {
                            // Enter löst kein Submit mehr aus.
                            if (event.key === 'Enter') {
                                event.preventDefault()
                            }
                        }}
                        value={title}
                    />
                </div>

                <div className="form-field">
                    <label htmlFor="edit-created-at">Erstellt am</label>
                    <input
                        disabled={loading}
                        id="edit-created-at"
                        onChange={event => setCreatedAt(event.target.value)}
                        onKeyDown={event => {
                            // Enter löst kein Submit mehr aus.
                            if (event.key === 'Enter') {
                                event.preventDefault()
                            }
                        }}
                        type="datetime-local"
                        value={createdAt ? createdAt.slice(0, 16) : ''}
                    />
                </div>

                <div className="form-row form-actions">
                    <button
                        disabled={loading || saving || !title.trim() || !createdAt.trim()}
                        type="submit"
                    >
                        {saving ? 'Speichert ...' : 'Speichern'}
                    </button>
                </div>
            </form>
        </>
    )
}

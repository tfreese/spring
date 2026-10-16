import type {SubmitEvent} from 'react'
import {useEffect, useState} from 'react'
import {Link, useNavigate, useParams} from 'react-router-dom'

import {useLayoutContext} from './layoutContext'
import type {Task} from './models/task'

export default function TaskEditPage() {
    // Wert des Pfad-Parameters ":id" aus der Route "/tasks/:id/edit".
    const {id} = useParams<{ id: string }>()

    const [title, setTitle] = useState('')
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const {setError} = useLayoutContext()
    const navigate = useNavigate()

    useEffect(() => {
    }, [])

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
        if (!normalizedTitle) return

        setSaving(true)
        setError(undefined)

        try {
            const response = await fetch(`/api/tasks/${id}`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({title: normalizedTitle}),
            })
            if (!response.ok) throw new Error(`HTTP ${response.status} - ${await response.text()}`)
            // Nach dem erfolgreichen Speichern zur Liste navigieren
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
                <label htmlFor="edit-title">Titel (Task {id})</label>
                <div className="form-row">
                    <input
                        disabled={loading}
                        id="edit-title"
                        maxLength={21}
                        onChange={event => setTitle(event.target.value)}
                        value={title}
                    />
                    <button disabled={loading || saving || !title.trim()} type="submit">
                        {saving ? 'Speichert ...' : 'Speichern'}
                    </button>
                </div>
            </form>
        </>
    )
}


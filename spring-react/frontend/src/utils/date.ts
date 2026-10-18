export function formatCreatedAt(value: string | null) {
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

export function formatDateOnly(value: string | null) {
    if (!value) return '—'

    try {
        return new Intl.DateTimeFormat('de-DE', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        }).format(new Date(value))
    } catch {
        return value
    }
}

export function formatTimeOnly(value: string | null) {
    if (!value) return '—'

    try {
        return new Intl.DateTimeFormat('de-DE', {
            hour: '2-digit',
            minute: '2-digit',
        }).format(new Date(value))
    } catch {
        return value
    }
}

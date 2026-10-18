const dateTimeFormatter = new Intl.DateTimeFormat('de-DE', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
})

const dateOnlyFormatter = new Intl.DateTimeFormat('de-DE', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
})

const timeOnlyFormatter = new Intl.DateTimeFormat('de-DE', {
    hour: '2-digit',
    minute: '2-digit',
})

export function formatDateTime(value: string | null) {
    if (!value) return '—'

    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value

    return dateTimeFormatter.format(date)
}

export function formatDateOnly(value: string | null) {
    if (!value) return '—'

    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value

    return dateOnlyFormatter.format(date)
}

export function formatTimeOnly(value: string | null) {
    if (!value) return '—'

    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value

    return timeOnlyFormatter.format(date)
}

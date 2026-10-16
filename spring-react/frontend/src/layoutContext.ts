import {useOutletContext} from 'react-router-dom'

export type LayoutContext = {
    setError: (message: string | undefined) => void
}

// Typisierter Zugriff auf den Context, den Layout über <Outlet context=…/> bereitstellt.
export function useLayoutContext() {
    return useOutletContext<LayoutContext>()
}


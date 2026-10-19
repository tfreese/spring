// '?url' bewirkt das diese Dateien im assets-Ordner landen.
import darkCss from '../styles/theme-darcula.css?url'
import lightCss from '../styles/theme-intellij-light.css?url'

export type ThemeName = 'dark' | 'light'

export const themeStyles: Record<ThemeName, string> = {
    dark: darkCss,
    light: lightCss,
}

export function applyTheme(theme: ThemeName) {
    let themeLink = document.getElementById('theme-css') as HTMLLinkElement | null

    if (!themeLink) {
        themeLink = document.createElement('link')
        themeLink.id = 'theme-css'
        themeLink.rel = 'stylesheet'
        document.head.appendChild(themeLink)
    }

    themeLink.href = themeStyles[theme]
    localStorage.setItem('theme', theme)
}

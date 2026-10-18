export type ThemeName = 'dark' | 'light'

export const themeStyles: Record<ThemeName, string> = {
    dark: '/src/styles/theme-darcula.css',
    light: '/src/styles/theme-intellij-light.css',
}

export function applyTheme(theme: ThemeName) {
    const themeLink = document.getElementById('theme-css') as HTMLLinkElement | null
    if (!themeLink) return

    themeLink.href = themeStyles[theme]
    localStorage.setItem('theme', theme)
}


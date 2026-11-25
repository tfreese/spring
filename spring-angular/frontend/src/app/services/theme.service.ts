import { Injectable, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private static readonly STORAGE_KEY = 'app-theme';

  readonly theme = signal<Theme>('light');

  constructor() {
    const storedTheme = this.readStoredTheme();
    this.applyTheme(storedTheme);
  }

  toggleTheme(): void {
    const nextTheme: Theme = this.theme() === 'light' ? 'dark' : 'light';
    this.applyTheme(nextTheme);
  }

  private applyTheme(theme: Theme): void {
    this.theme.set(theme);
    document.documentElement.setAttribute('data-theme', theme);

    try {
      localStorage.setItem(ThemeService.STORAGE_KEY, theme);
    } catch {
      // Ignore storage errors; theme still applies for the current session.
    }
  }

  private readStoredTheme(): Theme {
    try {
      const value = localStorage.getItem(ThemeService.STORAGE_KEY);
      if (value === 'dark' || value === 'light') {
        return value;
      }
    } catch {
      // Ignore storage errors and fall back to light theme.
    }

    return 'light';
  }
}


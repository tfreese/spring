import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from '../services/theme.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app-layout.component.html'
})
export class AppLayoutComponent {
  private readonly themeService = inject(ThemeService);

  get isDarkTheme(): boolean {
    return this.themeService.theme() === 'dark';
  }

  get themeAriaLabel(): string {
    return this.isDarkTheme
      ? 'Dunkles Theme ist aktiviert. Zum hellen Theme wechseln'
      : 'Helles Theme ist aktiviert. Zum dunklen Theme wechseln';
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}
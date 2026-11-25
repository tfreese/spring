import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { formatTaskCreatedAt } from '../../utils/task-date-time.util';
import { Task } from '../../models/task';
import { TaskApiService } from '../../services/task-api.service';

type SortableColumn = 'id' | 'title' | 'createdAt';

type TaskFilters = {
  id: string;
  title: string;
  createdAt: string;
};

@Component({
  selector: 'app-tasks-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './tasks-page.component.html',
  styleUrl: './tasks-page.component.css'
})
export class TasksPageComponent {
  private readonly taskApi = inject(TaskApiService);
  protected readonly formatCreatedAt = formatTaskCreatedAt;

  protected readonly tasks = signal<Task[]>([]);
  protected readonly title = signal('');
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);

  protected readonly sortColumn = signal<SortableColumn>('id');
  protected readonly sortDirection = signal<'asc' | 'desc'>('asc');
  protected readonly filters = signal<TaskFilters>({
    id: '',
    title: '',
    createdAt: ''
  });

  protected readonly visibleTasks = computed(() => {
    const { id, title, createdAt } = this.filters();
    const normalizedId = id.trim().toLowerCase();
    const normalizedTitle = title.trim().toLowerCase();
    const normalizedCreatedAt = createdAt.trim().toLowerCase();

    const filtered = this.tasks().filter((task) => {
      const matchesId = !normalizedId || String(task.id).includes(normalizedId);
      const matchesTitle = !normalizedTitle || task.title.toLowerCase().includes(normalizedTitle);
      const createdDisplay = this.formatCreatedAt(task.createdAt).toLowerCase();
      const createdRaw = (task.createdAt ?? '').toLowerCase();
      const matchesCreatedAt = !normalizedCreatedAt
        || createdDisplay.includes(normalizedCreatedAt)
        || createdRaw.includes(normalizedCreatedAt);

      return matchesId && matchesTitle && matchesCreatedAt;
    });

    const column = this.sortColumn();
    const direction = this.sortDirection();
    const directionFactor = direction === 'asc' ? 1 : -1;

    return [...filtered].sort((left, right) => {
      let compareResult: number;

      if (column === 'id') {
        compareResult = left.id - right.id;
      } else if (column === 'title') {
        compareResult = left.title.localeCompare(right.title, 'de');
      } else {
        compareResult = Date.parse(left.createdAt) - Date.parse(right.createdAt);
      }

      return compareResult * directionFactor;
    });
  });

  constructor() {
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);

    this.taskApi.loadTasks()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (tasks) => this.tasks.set(tasks),
        error: () => this.error.set('Tasks konnten nicht geladen werden.')
      });
  }

  protected createTask(): void {
    const trimmed = this.title().trim();

    if (!trimmed) {
      this.error.set('Bitte einen Titel eingeben.');
      this.success.set(null);
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    this.taskApi.createTask({ title: trimmed })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (createdTask) => {
          this.tasks.update((items) => [...items, createdTask]);
          this.title.set('');
          this.success.set('Task wurde erfolgreich angelegt.');
        },
        error: () => {
          this.error.set('Task konnte nicht gespeichert werden.');
          this.success.set(null);
        }
      });
  }

  protected deleteTask(id: number): void {
    this.taskApi.deleteTask(id).subscribe({
      next: () => {
        this.tasks.update((items) => items.filter((task) => task.id !== id));
        this.success.set('Task wurde gelöscht.');
      },
      error: () => {
        this.error.set('Task konnte nicht gelöscht werden.');
        this.success.set(null);
      }
    });
  }

  protected setFilter(column: keyof TaskFilters, value: string): void {
    this.filters.update((current) => ({
      ...current,
      [column]: value
    }));
  }

  protected clearFilter(column: keyof TaskFilters, input: HTMLInputElement): void {
    this.setFilter(column, '');
    input.focus();
  }

  protected toggleSort(column: SortableColumn): void {
    if (this.sortColumn() === column) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
      return;
    }

    this.sortColumn.set(column);
    this.sortDirection.set('asc');
  }

  protected ariaSortFor(column: SortableColumn): 'ascending' | 'descending' | 'none' {
    if (this.sortColumn() !== column) {
      return 'none';
    }

    return this.sortDirection() === 'asc' ? 'ascending' : 'descending';
  }

  protected sortIndicatorFor(column: SortableColumn): string {
    if (this.sortColumn() !== column) {
      return '↕';
    }

    return this.sortDirection() === 'asc' ? '▲' : '▼';
  }
}

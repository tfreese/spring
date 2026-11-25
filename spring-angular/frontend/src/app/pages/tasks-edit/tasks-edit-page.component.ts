import {CommonModule} from '@angular/common';
import {Component, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {finalize} from 'rxjs';
import {TaskApiService} from '../../services/task-api.service';
import {formatTaskCreatedAt} from '../../utils/task-date-time.util';

@Component({
    selector: 'app-tasks-edit-page',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './tasks-edit-page.component.html',
    styleUrl: './tasks-edit-page.component.css'
})
export class TasksEditPageComponent {
    private readonly route = inject(ActivatedRoute);
    private readonly taskApi = inject(TaskApiService);
    protected readonly formatCreatedAt = formatTaskCreatedAt;

    protected readonly taskId = signal<number | null>(null);
    protected readonly title = signal('');
    protected readonly createdAt = signal<string | null>(null);
    protected readonly loading = signal(false);
    protected readonly saving = signal(false);
    protected readonly error = signal<string | null>(null);
    protected readonly success = signal<string | null>(null);

    constructor() {
        this.loadTask();
    }

    protected save(): void {
        const id = this.taskId();
        const createdAt = this.createdAt();
        const trimmedTitle = this.title().trim();

        if (id === null || createdAt === null) {
            this.error.set('Task kann nicht gespeichert werden.');
            this.success.set(null);
            return;
        }

        if (!trimmedTitle) {
            this.error.set('Bitte einen Titel eingeben.');
            this.success.set(null);
            return;
        }

        this.saving.set(true);
        this.error.set(null);
        this.success.set(null);

        this.taskApi.update(id, {
            title: trimmedTitle,
            createdAt
        })
            .pipe(finalize(() => this.saving.set(false)))
            .subscribe({
                next: () => {
                    this.title.set(trimmedTitle);
                    this.success.set('Task wurde erfolgreich gespeichert.');
                },
                error: () => {
                    this.error.set('Task konnte nicht gespeichert werden.');
                    this.success.set(null);
                }
            });
    }

    private loadTask(): void {
        const idParam = this.route.snapshot.paramMap.get('id');
        const id = Number(idParam);

        if (!Number.isInteger(id) || id <= 0) {
            this.error.set('Ungültige Task-ID.');
            return;
        }

        this.taskId.set(id);
        this.loading.set(true);
        this.error.set(null);

        this.taskApi.findById(id)
            .pipe(finalize(() => this.loading.set(false)))
            .subscribe({
                next: (task) => {
                    this.title.set(task.title);
                    this.createdAt.set(task.createdAt);
                },
                error: () => {
                    this.error.set('Task konnte nicht geladen werden.');
                }
            });
    }
}


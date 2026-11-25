import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateTaskRequest, Task, UpdateTaskRequest } from '../models/task';

@Injectable({ providedIn: 'root' })
export class TaskApiService {
  private readonly http = inject(HttpClient);

  loadTasks(): Observable<Task[]> {
    return this.http.get<Task[]>('/api/tasks');
  }

  findById(id: number): Observable<Task> {
    return this.http.get<Task>(`/api/tasks/${id}`);
  }

  createTask(request: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>('/api/tasks', request);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`/api/tasks/${id}`);
  }

  update(id: number, request: UpdateTaskRequest): Observable<void> {
    return this.http.put<void>(`/api/tasks/${id}`, request);
  }
}


export interface Task {
  id: number;
  title: string;
  createdAt: string;
}

export interface CreateTaskRequest {
  title: string;
}

export interface UpdateTaskRequest {
  title: string;
  createdAt: string;
}


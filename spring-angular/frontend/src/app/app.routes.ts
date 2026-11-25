import { Routes } from '@angular/router';
import { AppLayoutComponent } from './layout/app-layout.component';
import { TasksPageComponent } from './pages/tasks/tasks-page.component';
import { TasksEditPageComponent } from './pages/tasks-edit/tasks-edit-page.component';

export const appRoutes: Routes = [
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      {
        path: '',
        component: TasksPageComponent
      },
      {
        path: 'tasks/:id/edit',
        component: TasksEditPageComponent
      }
    ]
  }
];

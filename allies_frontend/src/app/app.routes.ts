import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { VideoCallComponent } from './components/video-call/video-call.component'; // <-- thêm dòng này

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'video-call', component: VideoCallComponent },
  { path: '**', redirectTo: '/login' }
];


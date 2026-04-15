import { Component, OnInit } from '@angular/core';
import { AuthService, User } from '@core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  sampleData = [
    { id: 1, title: 'Sample Item 1', description: 'This is a sample item', status: 'Active' },
    { id: 2, title: 'Sample Item 2', description: 'Another sample item', status: 'Pending' },
    { id: 3, title: 'Sample Item 3', description: 'Third sample item', status: 'Complete' }
  ];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.authService.user$.subscribe(user => {
      this.currentUser = user;
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

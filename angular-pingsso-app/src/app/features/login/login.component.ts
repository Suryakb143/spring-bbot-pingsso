import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.handleAuthCallback();
  }

  private handleAuthCallback(): void {
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      const state = params['state'];

      if (code) {
        this.isLoading = true;
        this.authService.login(code).subscribe({
          next: () => {
            this.router.navigate(['/dashboard']);
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = 'Authentication failed. Please try again.';
            console.error('Authentication error:', error);
          }
        });
      }
    });
  }

  loginWithPingSso(): void {
    const authUrl = this.authService.getAuthorizationUrl();
    window.location.href = authUrl;
  }

  loginAsDemo(): void {
    this.isLoading = true;
    this.authService.getUserInfo().subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Demo login failed.';
      }
    });
  }
}

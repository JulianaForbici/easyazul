import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  email = '';
  senha = '';

  erro: string | null = null;
  carregando = false;

  constructor(private auth: AuthService) {}

  async entrar(): Promise<void> {
    this.erro = null;
    this.carregando = true;

    try {
      await this.auth.login({ email: this.email, senha: this.senha });
    } catch (e: unknown) {
      const err = e as HttpErrorResponse;

      const backendMsg =
        (err?.error && typeof err.error === 'object' && (err.error as any).message) ||
        (typeof err?.error === 'string' ? err.error : null);

      this.erro = backendMsg || 'Não foi possível fazer login. Verifique e-mail e senha.';
    } finally {
      this.carregando = false;
    }
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthUser, LoginRequest, LoginResponse, TipoUsuario } from './auth.models';
import { firstValueFrom } from 'rxjs';

const KEY = 'easyazul_auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  user: AuthUser | null = null;

  constructor(private http: HttpClient, private router: Router) {}

  hydrateFromStorage(): void {
    if (this.user) return;
    const raw = localStorage.getItem(KEY);
    if (!raw) return;
    try { this.user = JSON.parse(raw) as AuthUser; } catch { this.user = null; }
  }

  isAuthenticated(): boolean {
    this.hydrateFromStorage();
    return !!this.user?.token;
  }

  token(): string | null {
    this.hydrateFromStorage();
    return this.user?.token ?? null;
  }

  async login(payload: LoginRequest): Promise<void> {
    const res = await firstValueFrom(
      this.http.post<any>(`${environment.apiUrl}/login`, payload)
    );

    const idUsuario = Number(res?.idUsuario ?? res?.id ?? 0);

    const user: AuthUser = {
      token: String(res?.token ?? ''),
      idUsuario,
      nome: String(res?.nome ?? ''),
      tipo: res?.tipo as TipoUsuario,
    };

    this.user = user;
    localStorage.setItem(KEY, JSON.stringify(user));

    const tipo = String(user.tipo ?? '').toUpperCase();

    if (tipo === 'MOTORISTA' || tipo === 'EMPRESA') {
      this.router.navigateByUrl('/mapa');
    } else {
      this.router.navigateByUrl('/admin/usuarios');
    }
  }

  logout(): void {
    this.user = null;

    localStorage.removeItem(KEY);
    localStorage.removeItem('idVeiculoAtual');
    localStorage.removeItem('placaVeiculoAtual');
    for (let i = localStorage.length - 1; i >= 0; i--) {
      const k = localStorage.key(i);
      if (!k) continue;
      if (k.startsWith('easyazul:veiculoAtualId:') || k.startsWith('easyazul:veiculoAtualPlaca:')) {
        localStorage.removeItem(k);
      }
    }

    this.router.navigateByUrl('/login');
  }

  getTipo(): TipoUsuario | null {
    this.hydrateFromStorage();
    return this.user?.tipo ?? null;
  }

  isAdmin(): boolean {
    const tipo = this.getTipo();
    return tipo === 'ADMINISTRADOR';
  }

  isFiscalOuAdmin(): boolean {
    const tipo = this.getTipo();
    return tipo === 'ADMINISTRADOR' || tipo === 'FISCAL';
  }

  getIdUsuario(): number {
    this.hydrateFromStorage();
    return Number(this.user?.idUsuario ?? 0);
  }

  isUser(): boolean {
    const tipo = this.getTipo();
    return tipo === 'MOTORISTA' || tipo === 'EMPRESA';
  }

  isFiscal(): boolean {
    const tipo = this.getTipo();
    return tipo === 'FISCAL';
  }
}

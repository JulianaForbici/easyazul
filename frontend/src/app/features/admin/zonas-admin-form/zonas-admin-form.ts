import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ZonaService, ZonaUpdate } from '../../../core/api/zona.service/zona';

type ModoLocalizacao = 'ENDERECO' | 'LATLON';

@Component({
  selector: 'app-zonas-admin-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './zonas-admin-form.html',
  styleUrl: './zonas-admin-form.scss',
})
export class ZonasAdminFormComponent implements OnInit {
  protected carregando = false;
  protected erro: string | null = null;
  protected sucesso: string | null = null;

  protected modoLocalizacao: ModoLocalizacao = 'ENDERECO';

  protected nome = '';
  protected tarifa = '';
  protected tempoMaximo = '';
  protected horaInicio = '08:00';
  protected horaFim = '18:00';
  protected capacidadeVagas = '';

  protected descricao = '';
  protected endereco = '';
  protected latitude = '';
  protected longitude = '';

  constructor(
    private api: ZonaService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {}

  protected voltar(): void {
    this.router.navigateByUrl('/admin/zonas');
  }

  protected setModo(m: ModoLocalizacao): void {
    this.modoLocalizacao = m;
    this.erro = null;
    this.sucesso = null;
  }

  protected salvar(): void {
    this.erro = null;
    this.sucesso = null;

    const body = this.montarBody();
    if (!body) return;

    this.carregando = true;
    this.cdr.detectChanges();

    this.api
      .criar(body)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.sucesso = 'Zona criada com sucesso!';
          setTimeout(() => this.router.navigateByUrl('/admin/zonas'), 350);
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          const msg =
            (typeof e.error === 'string' && e.error) ||
            e.error?.message ||
            'Não foi possível criar a zona.';
          this.erro = msg;
        },
      });
  }

  private montarBody(): ZonaUpdate | null {
    const nome = (this.nome ?? '').trim().replace(/\s+/g, ' ');
    if (!nome) {
      this.erro = 'Informe o nome da zona.';
      return null;
    }

    const tarifaN = this.parseNumber(this.tarifa);
    if (tarifaN === null || tarifaN < 0) {
      this.erro = 'Informe uma tarifa válida.';
      return null;
    }

    const tempoN = this.parseIntSafe(this.tempoMaximo);
    if (tempoN === null || tempoN <= 0) {
      this.erro = 'Informe o tempo máximo (min) maior que zero.';
      return null;
    }

    if (!this.horaInicio || !this.horaFim) {
      this.erro = 'Informe hora de início e hora de fim.';
      return null;
    }

    const capN = this.parseIntSafe(this.capacidadeVagas);
    if (capN === null || capN < 0) {
      this.erro = 'Capacidade de vagas inválida.';
      return null;
    }

    if (this.modoLocalizacao === 'LATLON') {
      const latN = this.parseNumber(this.latitude);
      const lonN = this.parseNumber(this.longitude);
      if (latN === null || lonN === null) {
        this.erro = 'Informe latitude e longitude.';
        return null;
      }

      return {
        nome,
        tarifa: tarifaN,
        tempoMaximo: tempoN,
        horaInicio: this.horaInicio,
        horaFim: this.horaFim,
        capacidadeVagas: capN,
        descricao: this.safeText(this.descricao),
        latitude: latN,
        longitude: lonN,
        endereco: null,
      };
    }

    const end = (this.endereco ?? '').trim();
    if (!end) {
      this.erro = 'Informe um endereço para localizar a zona.';
      return null;
    }

    return {
      nome,
      tarifa: tarifaN,
      tempoMaximo: tempoN,
      horaInicio: this.horaInicio,
      horaFim: this.horaFim,
      capacidadeVagas: capN,
      descricao: this.safeText(this.descricao),
      endereco: end,
      latitude: null,
      longitude: null,
    };
  }

  private safeText(v: string): string | null {
    const s = (v ?? '').trim();
    return s ? s : null;
  }

  private parseNumber(v: string): number | null {
    const s = String(v ?? '').trim().replace(',', '.');
    if (!s) return null;
    const n = Number(s);
    return Number.isFinite(n) ? n : null;
  }

  private parseIntSafe(v: string): number | null {
    const s = String(v ?? '').trim();
    if (!s) return null;
    const n = Number(s);
    if (!Number.isFinite(n)) return null;
    return Math.trunc(n);
  }
}

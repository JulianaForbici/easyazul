import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import {Zona, ZonaService, ZonaUpdate} from '../../../core/api/zona.service/zona';

type ModoLocalizacao = 'ENDERECO' | 'LATLON';

@Component({
  selector: 'app-zonas-admin-edit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './zonas-admin-edit.html',
  styleUrl: './zonas-admin-edit.scss',
})
export class ZonasAdminEditComponent implements OnInit {
  protected carregando = false;
  protected salvando = false;
  protected erro: string | null = null;
  protected okMsg: string | null = null;
  protected idZona = 0;
  protected zonaOriginal: Zona | null = null;
  protected nome = '';
  protected tarifa: number | null = null;
  protected descricao = '';
  protected tempoMaximo: number | null = null;
  protected horaInicio = '';
  protected horaFim = '';
  protected capacidadeVagas: number | null = null;
  protected modoLocalizacao: ModoLocalizacao = 'ENDERECO';
  protected endereco = '';
  protected latitude: number | null = null;
  protected longitude: number | null = null;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ZonaService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.idZona = Number(this.route.snapshot.paramMap.get('id') ?? 0);
    if (!this.idZona) {
      this.erro = 'ID da zona inválido.';
      return;
    }
    this.carregar();
  }

  protected voltar(): void {
    this.router.navigateByUrl('/admin/zonas');
  }

  protected carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.okMsg = null;

    this.api
      .detalhar(this.idZona)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (z) => {
          this.zonaOriginal = z;
          this.hidratarForm(z);
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          this.erro = e?.error?.message ?? 'Não foi possível carregar a zona.';
        },
      });
  }

  private hidratarForm(z: Zona): void {
    this.nome = String(z?.nome ?? '');
    this.tarifa = z?.tarifa ?? null;
    this.descricao = String(z?.descricao ?? '');
    this.tempoMaximo = z?.tempoMaximo ?? null;
    this.horaInicio = this.hhmm(z?.horaInicio);
    this.horaFim = this.hhmm(z?.horaFim);
    this.capacidadeVagas = z?.capacidadeVagas ?? null;

    const temLatLon = this.isNum(z?.latitude) && this.isNum(z?.longitude);
    if (temLatLon) {
      this.modoLocalizacao = 'LATLON';
      this.latitude = Number(z.latitude);
      this.longitude = Number(z.longitude);
      this.endereco = String((z as any)?.endereco ?? '');
    } else {
      this.modoLocalizacao = 'ENDERECO';
      this.endereco = String((z as any)?.endereco ?? '');
      this.latitude = null;
      this.longitude = null;
    }
  }

  private hhmm(v: any): string {
    if (!v) return '';
    const s = String(v);
    if (s.includes(':')) return s.slice(0, 5);
    return s;
  }

  private isNum(v: any): boolean {
    const n = Number(v);
    return Number.isFinite(n);
  }

  protected trocarModo(m: ModoLocalizacao): void {
    this.modoLocalizacao = m;
    this.okMsg = null;
    this.erro = null;
  }

  protected salvar(): void {
    const falhar = (msg: string): void => {
      this.erro = msg;
      this.okMsg = null;
    };

    this.erro = null;
    this.okMsg = null;

    const body: any = {
      nome: this.nome?.trim() || null,
      tarifa: this.tarifa ?? null,
      descricao: this.descricao?.trim() ? this.descricao.trim() : null,
      tempoMaximo: this.tempoMaximo ?? null,
      horaInicio: this.horaInicio || null,
      horaFim: this.horaFim || null,
      capacidadeVagas: this.capacidadeVagas ?? null,
      endereco: null,
      latitude: null,
      longitude: null,
    };

    if (this.modoLocalizacao === 'LATLON') {
      body.latitude = this.latitude ?? null;
      body.longitude = this.longitude ?? null;
    } else {
      body.endereco = this.endereco?.trim() || null;
    }

    if (!body.nome) return falhar('Informe o nome da zona.');
    if (body.tarifa === null || Number(body.tarifa) < 0) return falhar('Informe uma tarifa válida.');
    if (!body.tempoMaximo || Number(body.tempoMaximo) <= 0) return falhar('Informe o tempo máximo.');
    if (!body.horaInicio || !body.horaFim) return falhar('Informe hora início e hora fim.');
    if (body.capacidadeVagas === null || Number(body.capacidadeVagas) < 0) return falhar('Capacidade inválida.');
    if (this.modoLocalizacao === 'LATLON') {
      if (body.latitude === null || body.longitude === null) return falhar('Informe latitude e longitude.');
    } else {
      if (!body.endereco) return falhar('Informe um endereço.');
    }

    this.salvando = true;

    this.api.atualizar(this.idZona, body)
      .pipe(finalize(() => {
        this.salvando = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.okMsg = 'Zona atualizada com sucesso.';
          setTimeout(() => this.voltar(), 450);
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          this.erro = e?.error?.message ?? 'Não foi possível salvar.';
        },
      });
  }
}

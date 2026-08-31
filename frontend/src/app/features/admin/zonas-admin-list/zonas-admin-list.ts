import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import {Page, StatusZona, Zona, ZonaService} from '../../../core/api/zona.service/zona';

type FiltroStatus = 'TODAS' | 'ATIVAS' | 'INATIVAS';

@Component({
  selector: 'app-zonas-admin-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './zonas-admin-list.html',
  styleUrl: './zonas-admin-list.scss',
})
export class ZonasAdminListComponent implements OnInit {
  protected zonas: Zona[] = [];
  protected zonasFiltradas: Zona[] = [];
  protected carregando = false;
  protected erro: string | null = null;
  protected busca = '';
  protected filtroStatus: FiltroStatus = 'TODAS';
  protected page = 0;
  protected size = 10;
  protected pageData: Page<Zona> | null = null;
  protected modalAberto = false;
  protected modalTitulo = '';
  protected modalHtml = '';
  protected acaoConfirmar: (() => void) | null = null;

  constructor(
    private api: ZonaService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  protected get isFirst(): boolean {
    return !!this.pageData?.first;
  }

  protected get isLast(): boolean {
    return !!this.pageData?.last;
  }

  protected prev(): void {
    if (this.page <= 0) return;
    this.page--;
    this.carregar();
  }

  protected next(): void {
    if (this.pageData?.totalPages && this.page >= this.pageData.totalPages - 1) return;
    this.page++;
    this.carregar();
  }

  protected carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.cdr.detectChanges();

    this.api
      .listarAdmin(this.page, this.size)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (res: Page<Zona>) => {
          this.pageData = res;
          this.zonas = res?.content ?? [];
          this.aplicarFiltros();
        },
        error: (err: unknown) => {
          console.error(err);
          this.erro = 'Não foi possível carregar a listagem de zonas.';
          this.pageData = null;
          this.zonas = [];
          this.zonasFiltradas = [];},});
  }

  protected setFiltro(f: FiltroStatus): void {
    this.filtroStatus = f;
    this.aplicarFiltros();
  }

  protected aplicarBusca(): void {
    this.aplicarFiltros();
  }

  private aplicarFiltros(): void {
    const q = (this.busca ?? '').trim().toLowerCase();

    const base = !q
      ? this.zonas
      : this.zonas.filter((z) => {
        const nome = String(this.nomeOf(z)).toLowerCase();
        const desc = String(z?.descricao ?? '').toLowerCase();
        const end = String(z?.endereco ?? '').toLowerCase();
        return nome.includes(q) || desc.includes(q) || end.includes(q);
      });

    const filtradas = base.filter((z) => {
      if (this.filtroStatus === 'TODAS') return true;
      if (this.filtroStatus === 'ATIVAS') return this.statusZonaOf(z) === 'ATIVA';
      return this.statusZonaOf(z) === 'INATIVA';
    });

    this.zonasFiltradas = filtradas;
  }

  protected getId(z: Zona): number {
    return Number(z?.id ?? z?.idZona ?? 0);
  }

  protected nomeOf(z: Zona): string {
    return String(z?.nome ?? 'Zona');
  }

  protected statusZonaOf(z: Zona): StatusZona {
    const raw = (z as any)?.statusZona ?? (z as any)?.status ?? null;
    const s = String(raw ?? '').toUpperCase();
    if (s.includes('INATIV')) return 'INATIVA';
    if (s.includes('ATIV')) return 'ATIVA';
    return 'ATIVA';
  }

  protected statusLabel(z: Zona): string {
    return this.statusZonaOf(z) === 'INATIVA' ? 'Inativa' : 'Ativa';
  }

  protected statusPillClass(z: Zona): 'ok' | 'bad' {
    return this.statusZonaOf(z) === 'INATIVA' ? 'bad' : 'ok';
  }

  protected tarifaLabel(z: Zona): string {
    const v = Number((z as any)?.tarifa ?? 0);
    if (!Number.isFinite(v) || v <= 0) return '—';
    return `R$ ${v.toFixed(2).replace('.', ',')}`;
  }

  protected tempoLabel(z: Zona): string {
    const t = Number((z as any)?.tempoMaximo ?? 0);
    if (!Number.isFinite(t) || t <= 0) return '—';
    return `${t} min`;
  }

  protected horaLabel(h: any): string {
    if (!h) return '—';

    if (typeof h === 'string') {
      const s = h.trim();
      if (!s) return '—';
      return s.length >= 5 ? s.slice(0, 5) : s;
    }

    const hour = Number(h?.hour);
    const minute = Number(h?.minute);
    if (Number.isFinite(hour) && Number.isFinite(minute)) {
      const hh = String(hour).padStart(2, '0');
      const mm = String(minute).padStart(2, '0');
      return `${hh}:${mm}`;
    }
    return '—';
  }

  protected horarioLabel(z: Zona): string {
    const ini = this.horaLabel((z as any)?.horaInicio);
    const fim = this.horaLabel((z as any)?.horaFim);
    if (ini === '—' && fim === '—') return '—';
    return `${ini} – ${fim}`;
  }

  protected capacidadeLabel(z: Zona): string {
    const c = Number((z as any)?.capacidadeVagas ?? 0);
    if (!Number.isFinite(c) || c < 0) return '—';
    return String(c);
  }

  protected novo(): void {
    this.router.navigateByUrl('/admin/zonas/novo');
  }

  protected editar(z: Zona): void {
    const id = this.getId(z);
    if (!id) return;

    this.router.navigateByUrl(`/admin/zonas/${id}/editar`);
  }

  protected detalhar(z: Zona): void {
    const id = this.getId(z);
    if (!id) return;

    this.modalTitulo = `Detalhes da zona`;
    this.modalHtml = `
      <div class="md">
        <div><b>ID:</b> ${id}</div>
        <div><b>Nome:</b> ${this.escapeHtml(this.nomeOf(z))}</div>
        <div><b>Status:</b> ${this.statusLabel(z)}</div>
        <div><b>Tarifa:</b> ${this.tarifaLabel(z)}</div>
        <div><b>Tempo máximo:</b> ${this.tempoLabel(z)}</div>
        <div><b>Horário:</b> ${this.horarioLabel(z)}</div>
        <div><b>Capacidade:</b> ${this.capacidadeLabel(z)}</div>
        ${
      (z as any)?.descricao
        ? `<div style="margin-top:10px;"><b>Descrição:</b><br/>${this.escapeHtml(String((z as any)?.descricao))}</div>`
        : ''
    }
      </div>
    `;

    this.acaoConfirmar = null;
    this.modalAberto = true;
  }

  protected inativar(z: Zona): void {
    const id = this.getId(z);
    if (!id) return;

    if (this.statusZonaOf(z) === 'INATIVA') {
      this.modalTitulo = 'Atenção';
      this.modalHtml = `Essa zona já está <b>inativa</b>.`;
      this.acaoConfirmar = null;
      this.modalAberto = true;
      return;
    }

    const nome = this.escapeHtml(this.nomeOf(z));

    this.modalTitulo = 'Inativar zona';
    this.modalHtml = `
      <div class="md">
        Tem certeza que deseja inativar a zona <b>${nome}</b>?
      </div>
    `;

    this.acaoConfirmar = () => {
      this.carregando = true;
      this.erro = null;
      this.cdr.detectChanges();

      this.api
        .excluir(id)
        .pipe(
          finalize(() => {
            this.carregando = false;
            this.cdr.detectChanges();
          })
        )
        .subscribe({
          next: () => {
            this.fecharModal();
            this.carregar();
          },
          error: (e: HttpErrorResponse) => {
            console.error(e);
            const msg =
              (typeof e.error === 'string' && e.error) ||
              e.error?.message ||
              'Não foi possível inativar a zona.';
            this.modalHtml = `
              <div class="md">
                <div class="err">${this.escapeHtml(msg)}</div>
              </div>
            `;
            this.acaoConfirmar = null;

            if (e.status === 422) this.carregar();
          },
        });
    };

    this.modalAberto = true;
  }

  protected fecharModal(): void {
    this.modalAberto = false;
    this.modalTitulo = '';
    this.modalHtml = '';
    this.acaoConfirmar = null;
  }

  protected trackById = (_: number, z: Zona) => this.getId(z) || this.nomeOf(z);

  private escapeHtml(v: string): string {
    return String(v ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  protected get totalAtivas(): number {
    return (this.zonas ?? []).filter((z) => this.statusZonaOf(z) === 'ATIVA').length;
  }

  protected get totalInativas(): number {
    return (this.zonas ?? []).filter((z) => this.statusZonaOf(z) === 'INATIVA').length;
  }

  protected get totalElements(): number {
    return Number(this.pageData?.totalElements ?? 0);
  }
}

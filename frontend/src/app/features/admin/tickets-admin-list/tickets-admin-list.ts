import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { Page, StatusTicket, Ticket, TicketService } from '../../../core/api/ticket.service/ticket';
import { AuthService } from '../../../core/auth/auth.service';

type FiltroStatus = 'TODOS' | 'ATIVO' | 'RESERVADO' | 'FECHADO' | 'CANCELADO';

@Component({
  selector: 'app-tickets-admin-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tickets-admin-list.html',
  styleUrl: './tickets-admin-list.scss',
})
export class TicketsAdminListComponent implements OnInit {
  protected ticketsAll: Ticket[] = [];
  protected ticketsFiltrados: Ticket[] = [];

  protected carregando = false;
  protected erro: string | null = null;

  protected busca = '';
  protected filtroStatus: FiltroStatus = 'TODOS';

  protected page = 0;
  protected size = 10;
  protected pageData: Page<Ticket> | null = null;

  protected modalAberto = false;
  protected modalTitulo = '';
  protected modalHtml = '';
  protected acaoConfirmar: (() => void) | null = null;
  protected textoConfirmar = 'Confirmar';

  constructor(
    private api: TicketService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  protected get isFirst(): boolean {
    return this.pageData ? !!(this.pageData as any).first : true;
  }

  protected get isLast(): boolean {
    return this.pageData ? !!(this.pageData as any).last : true;
  }

  protected prev(): void {
    if (this.page <= 0) return;
    this.page--;
    this.aplicarFiltrosEPaginar();
  }

  protected next(): void {
    if (!this.pageData) return;
    const tp = Number((this.pageData as any)?.totalPages ?? 0);
    if (!tp || this.page >= tp - 1) return;
    this.page++;
    this.aplicarFiltrosEPaginar();
  }

  protected carregar(): void {
    this.carregarTudo();
  }

  private carregarTudo(): void {
    this.carregando = true;
    this.erro = null;
    this.cdr.detectChanges();

    this.api
      .listarTodos(0, 5000)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (res) => {
          this.ticketsAll = res?.content ?? [];
          this.page = 0;
          this.aplicarFiltrosEPaginar();
        },
        error: (err: unknown) => {
          console.error(err);
          this.erro = 'Não foi possível carregar a lista de tickets (admin).';
          this.pageData = null;
          this.ticketsAll = [];
          this.ticketsFiltrados = [];
        },
      });
  }

  protected setFiltro(f: FiltroStatus): void {
    this.filtroStatus = f;
    this.page = 0;
    this.aplicarFiltrosEPaginar();
  }

  protected aplicarBusca(): void {
    this.page = 0;
    this.aplicarFiltrosEPaginar();
  }

  private aplicarFiltrosEPaginar(): void {
    const q = (this.busca ?? '').trim().toLowerCase();

    const base = !q
      ? this.ticketsAll
      : this.ticketsAll.filter((t) => {
          const placa = String(this.placaOf(t)).toLowerCase();
          const zona = String(this.zonaOf(t)).toLowerCase();
          const dono = String(this.nomeDonoOf(t)).toLowerCase();
          const status = String(this.statusOf(t)).toLowerCase();
          return placa.includes(q) || zona.includes(q) || dono.includes(q) || status.includes(q);
        });

    const filtrados = base.filter((t) => {
      if (this.filtroStatus === 'TODOS') return true;
      return this.statusOf(t) === this.filtroStatus;
    });

    const total = filtrados.length;
    const totalPages = total === 0 ? 0 : Math.ceil(total / this.size);

    if (totalPages === 0) {
      this.page = 0;
      this.ticketsFiltrados = [];
      this.pageData = {
        content: [],
        number: 0,
        size: this.size,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
      } as any;
      this.cdr.detectChanges();
      return;
    }

    if (this.page > totalPages - 1) this.page = totalPages - 1;

    const start = this.page * this.size;
    const end = start + this.size;
    this.ticketsFiltrados = filtrados.slice(start, end);

    this.pageData = {
      content: this.ticketsFiltrados,
      number: this.page,
      size: this.size,
      totalElements: total,
      totalPages,
      first: this.page === 0,
      last: this.page >= totalPages - 1,
    } as any;

    this.cdr.detectChanges();
  }

  protected getId(t: Ticket): number {
    return Number((t as any)?.id ?? (t as any)?.idTicket ?? 0);
  }

  protected placaOf(t: Ticket): string {
    return String((t as any)?.placa ?? '—').toUpperCase();
  }

  protected zonaOf(t: Ticket): string {
    return String((t as any)?.nomeZona ?? 'Zona');
  }

  protected nomeDonoOf(t: Ticket): string {
    return String((t as any)?.nomeDono ?? '—');
  }

  protected statusOf(t: Ticket): StatusTicket {
    const raw = (t as any)?.status ?? '';
    const s = String(raw).toUpperCase();
    if (s.includes('RESERV')) return 'RESERVADO';
    if (s.includes('FECH')) return 'FECHADO';
    if (s.includes('CANC')) return 'CANCELADO';
    return 'ATIVO';
  }

  protected isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  protected statusLabel(t: Ticket): string {
    const s = this.statusOf(t);
    if (s === 'RESERVADO') return 'Reservado';
    if (s === 'FECHADO') return 'Fechado';
    if (s === 'CANCELADO') return 'Cancelado';
    return 'Ativo';
  }

  protected statusPillClass(t: Ticket): 'ok' | 'warn' | 'bad' | 'muted' {
    const s = this.statusOf(t);
    if (s === 'ATIVO') return 'ok';
    if (s === 'RESERVADO') return 'warn';
    if (s === 'FECHADO') return 'muted';
    return 'bad';
  }

  protected inicioLabel(t: Ticket): string {
    return this.formatDateTime((t as any)?.inicioTicket);
  }

  protected fimLabel(t: Ticket): string {
    return this.formatDateTime((t as any)?.fimTicket);
  }

  protected valorLabel(t: Ticket): string {
    const v = Number((t as any)?.valor ?? 0);
    if (!Number.isFinite(v) || v <= 0) return '—';
    return `R$ ${v.toFixed(2).replace('.', ',')}`;
  }

  private formatDateTime(v: any): string {
    if (!v) return '—';
    const s = String(v).trim();
    if (s.includes('T')) {
      const [d, time] = s.split('T');
      const hhmm = (time ?? '').slice(0, 5);
      const [yyyy, mm, dd] = d.split('-');
      if (yyyy && mm && dd) return `${dd}/${mm}/${yyyy} ${hhmm}`;
      return `${d} ${hhmm}`;
    }
    return s;
  }

  protected podeIniciar(t: Ticket): boolean {
    return this.statusOf(t) === 'RESERVADO';
  }

  protected podeFechar(t: Ticket): boolean {
    return this.statusOf(t) === 'ATIVO';
  }

  protected podeCancelar(t: Ticket): boolean {
    const s = this.statusOf(t);
    return this.isAdmin() && (s === 'ATIVO' || s === 'RESERVADO');
  }

  protected detalhar(t: Ticket): void {
    const id = this.getId(t);
    if (!id) return;

    this.modalTitulo = 'Detalhes do ticket';
    this.modalHtml = `
      <div class="md">
        <div><b>ID:</b> ${id}</div>
        <div><b>Placa:</b> ${this.escapeHtml(this.placaOf(t))}</div>
        <div><b>Dono:</b> ${this.escapeHtml(this.nomeDonoOf(t))} (ID ${(t as any)?.idDono ?? '—'})</div>
        <div><b>Zona:</b> ${this.escapeHtml(this.zonaOf(t))} (ID ${(t as any)?.idZona ?? '—'})</div>
        <div><b>Status:</b> ${this.statusLabel(t)}</div>
        <div><b>Início:</b> ${this.inicioLabel(t)}</div>
        <div><b>Fim:</b> ${this.fimLabel(t)}</div>
        <div><b>Valor:</b> ${this.valorLabel(t)}</div>
      </div>
    `;
    this.acaoConfirmar = null;
    this.textoConfirmar = 'Ok';
    this.modalAberto = true;
  }

  protected iniciar(t: Ticket): void {
    const id = this.getId(t);
    if (!id) return;

    this.modalTitulo = 'Iniciar ticket';
    this.modalHtml = `<div class="md">Deseja iniciar o ticket da placa <b>${this.escapeHtml(this.placaOf(t))}</b>?</div>`;
    this.textoConfirmar = 'Iniciar';

    this.acaoConfirmar = () => {
      this.carregando = true;
      this.erro = null;
      this.cdr.detectChanges();

      this.api
        .iniciar(id)
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
          error: (e: HttpErrorResponse) => this.renderErroNoModal(e, 'Não foi possível iniciar.'),
        });
    };

    this.modalAberto = true;
  }

  protected fechar(t: Ticket): void {
    const id = this.getId(t);
    if (!id) return;

    this.modalTitulo = 'Fechar ticket';
    this.modalHtml = `<div class="md">Deseja fechar o ticket da placa <b>${this.escapeHtml(this.placaOf(t))}</b>?</div>`;
    this.textoConfirmar = 'Fechar';

    this.acaoConfirmar = () => {
      this.carregando = true;
      this.erro = null;
      this.cdr.detectChanges();

      this.api
        .fechar(id)
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
          error: (e: HttpErrorResponse) => this.renderErroNoModal(e, 'Não foi possível fechar.'),
        });
    };

    this.modalAberto = true;
  }

  protected cancelar(t: Ticket): void {
    const id = this.getId(t);
    if (!id) return;

    this.modalTitulo = 'Cancelar ticket';
    this.modalHtml = `
      <div class="md">
        Tem certeza que deseja cancelar o ticket da placa <b>${this.escapeHtml(this.placaOf(t))}</b>?
        <div class="hint">Esta ação não pode ser desfeita.</div>
      </div>
    `;
    this.textoConfirmar = 'Cancelar';

    this.acaoConfirmar = () => {
      this.carregando = true;
      this.erro = null;
      this.cdr.detectChanges();

      this.api
        .cancelar(id)
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
          error: (e: HttpErrorResponse) => this.renderErroNoModal(e, 'Não foi possível cancelar.'),
        });
    };

    this.modalAberto = true;
  }

  private renderErroNoModal(e: HttpErrorResponse, fallback: string): void {
    console.error(e);
    const msg = (typeof e.error === 'string' && e.error) || (e.error as any)?.message || fallback;
    this.modalHtml = `<div class="md"><div class="err">${this.escapeHtml(msg)}</div></div>`;
    this.acaoConfirmar = null;
    this.textoConfirmar = 'Ok';
  }

  protected fecharModal(): void {
    this.modalAberto = false;
    this.modalTitulo = '';
    this.modalHtml = '';
    this.acaoConfirmar = null;
    this.textoConfirmar = 'Confirmar';
  }

  protected voltarAdminHome(): void {
    this.router.navigateByUrl('/admin/usuarios');
  }

  protected trackById = (_: number, t: Ticket) => this.getId(t) || this.placaOf(t);

  private escapeHtml(v: string): string {
    return String(v ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  protected get totalAtivos(): number {
    return (this.ticketsAll ?? []).filter((t) => this.statusOf(t) === 'ATIVO').length;
  }

  protected get totalReservados(): number {
    return (this.ticketsAll ?? []).filter((t) => this.statusOf(t) === 'RESERVADO').length;
  }

  protected get totalFechados(): number {
    return (this.ticketsAll ?? []).filter((t) => this.statusOf(t) === 'FECHADO').length;
  }

  protected get totalCancelados(): number {
    return (this.ticketsAll ?? []).filter((t) => this.statusOf(t) === 'CANCELADO').length;
  }
}

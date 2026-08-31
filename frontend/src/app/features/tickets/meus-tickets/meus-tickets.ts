import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { Page, StatusTicket, Ticket, TicketService } from '../../../core/api/ticket.service/ticket';
import {
  CriarPagamentoPayload,
  FormaPagamento,
  Pagamento,
  PagamentoService,
} from '../../../core/api/pagamento.service/pagamento';
import { AuthService } from '../../../core/auth/auth.service';

type FiltroStatus = 'TODOS' | 'ATIVO' | 'RESERVADO' | 'FECHADO' | 'CANCELADO';

@Component({
  selector: 'app-meus-tickets',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './meus-tickets.html',
  styleUrl: './meus-tickets.scss',
})
export class MeusTicketsComponent implements OnInit {
  protected tickets: Ticket[] = [];
  protected ticketsAll: Ticket[] = [];
  protected ticketsFiltrados: Ticket[] = [];

  protected carregando = false;
  protected erro: string | null = null;

  protected busca = '';
  protected filtroStatus: FiltroStatus = 'TODOS';

  protected page = 0;
  protected size = 10;
  protected pageData: Page<Ticket> | null = null;

  protected ticketsComPagamentoEmAberto = new Set<number>();

  protected modalAberto = false;
  protected modalTitulo = '';
  protected modalHtml = '';
  protected acaoConfirmar: (() => void) | null = null;
  protected textoConfirmar = 'Confirmar';

  protected pagarAberto = false;
  protected ticketPagar: Ticket | null = null;
  protected formaPagamento: FormaPagamento = 'PIX';
  protected codigoReferencia = '';
  protected observacaoPagamento = '';
  protected pagando = false;
  protected pagarErro: string | null = null;

  protected formaMenuAberto = false;

  protected readonly formasPagamento: FormaPagamento[] = [
    'PIX',
    'CREDITO',
    'DEBITO',
    'DINHEIRO',
    'BOLETO',
    'TRANSFERENCIA',
  ];

  constructor(
    private api: TicketService,
    private pagamentosApi: PagamentoService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.carregarPagamentosEmAberto();
    this.carregar();
  }

  private carregarPagamentosEmAberto(): void {
    this.ticketsComPagamentoEmAberto.clear();

    this.pagamentosApi.listarMeus(0, 2000).subscribe({
      next: (res) => {
        const content: Pagamento[] = (res as any)?.content ?? [];
        for (const p of content) {
          const status = String((p as any)?.status ?? '').toUpperCase();
          const idTicket = Number((p as any)?.idTicket ?? 0);
          if (!idTicket) continue;
          if (status === 'PENDENTE' || status === 'APROVADO') {
            this.ticketsComPagamentoEmAberto.add(idTicket);
          }
        }
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
      },
    });
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
      .listarMeus(0, 5000)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (res) => {
          this.ticketsAll = res?.content ?? [];
          this.tickets = this.ticketsAll;
          this.page = 0;
          this.aplicarFiltrosEPaginar();
        },
        error: (err: unknown) => {
          console.error(err);
          this.erro = 'Não foi possível carregar seus tickets.';
          this.pageData = null;
          this.ticketsAll = [];
          this.tickets = [];
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
          const status = String(this.statusOf(t)).toLowerCase();
          return placa.includes(q) || zona.includes(q) || status.includes(q);
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

  protected ticketCodigo(t: Ticket): string {
    return this.ticketCodigoFromId(this.getId(t));
  }

  private ticketCodigoFromId(id: number): string {
    const n = Number(id || 0);
    if (!n) return '—';
    return `TCK-${String(n).padStart(6, '0')}`;
  }

  protected placaOf(t: Ticket): string {
    return String((t as any)?.placa ?? '—').toUpperCase();
  }

  protected zonaOf(t: Ticket): string {
    return String((t as any)?.nomeZona ?? 'Zona');
  }

  protected statusOf(t: Ticket): StatusTicket {
    const raw = (t as any)?.status ?? '';
    const s = String(raw).toUpperCase();
    if (s.includes('RESERV')) return 'RESERVADO';
    if (s.includes('FECH')) return 'FECHADO';
    if (s.includes('CANC')) return 'CANCELADO';
    return 'ATIVO';
  }

  protected statusLabel(t: Ticket): string {
    const s = this.statusOf(t);
    if (s === 'RESERVADO') return 'Reservado';
    if (s === 'FECHADO') return 'Fechado';
    if (s === 'CANCELADO') return 'Cancelado';
    return 'Ativo';
  }

  protected isAdmin(): boolean {
    return this.auth.isAdmin();
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

  protected podePagar(t: Ticket): boolean {
    return this.statusOf(t) === 'FECHADO' && !this.temPagamentoEmAberto(t);
  }

  protected exibePagar(t: Ticket): boolean {
    return this.statusOf(t) === 'FECHADO';
  }

  protected temPagamentoEmAberto(t: Ticket): boolean {
    const id = this.getId(t);
    if (!id) return false;
    return this.ticketsComPagamentoEmAberto.has(id);
  }

  protected pagarTitle(t: Ticket): string {
    return this.temPagamentoEmAberto(t)
      ? 'Esse ticket já possui pagamento PENDENTE ou APROVADO.'
      : 'Criar pagamento para este ticket.';
  }

  protected iniciar(t: Ticket): void {
    const id = this.getId(t);
    if (!id) return;

    this.modalTitulo = 'Iniciar ticket';
    this.modalHtml = `
      <div class="md">
        Deseja iniciar o ticket da placa <b>${this.escapeHtml(this.placaOf(t))}</b> na zona <b>${this.escapeHtml(
      this.zonaOf(t)
    )}</b>?
      </div>
    `;
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
          error: (e: HttpErrorResponse) => {
            console.error(e);
            const msg =
              (typeof e.error === 'string' && e.error) ||
              (e.error as any)?.message ||
              'Não foi possível iniciar.';
            this.modalHtml = `<div class="md"><div class="err">${this.escapeHtml(msg)}</div></div>`;
            this.acaoConfirmar = null;
            this.textoConfirmar = 'Ok';
          },
        });
    };

    this.modalAberto = true;
  }

  protected fechar(t: Ticket): void {
    const id = this.getId(t);
    if (!id) return;

    this.modalTitulo = 'Fechar ticket';
    this.modalHtml = `
      <div class="md">
        Deseja fechar o ticket da placa <b>${this.escapeHtml(this.placaOf(t))}</b>?
        <div class="hint">O valor será calculado conforme a tarifa e duração.</div>
      </div>
    `;
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
          error: (e: HttpErrorResponse) => {
            console.error(e);
            const msg =
              (typeof e.error === 'string' && e.error) ||
              (e.error as any)?.message ||
              'Não foi possível fechar.';
            this.modalHtml = `<div class="md"><div class="err">${this.escapeHtml(msg)}</div></div>`;
            this.acaoConfirmar = null;
            this.textoConfirmar = 'Ok';
          },
        });
    };

    this.modalAberto = true;
  }

  protected abrirPagar(t: Ticket): void {
    if (!this.podePagar(t)) return;

    this.ticketPagar = t;
    this.formaPagamento = 'PIX';
    this.codigoReferencia = '';
    this.observacaoPagamento = '';
    this.pagarErro = null;
    this.pagando = false;
    this.formaMenuAberto = false;
    this.pagarAberto = true;
  }

  protected fecharPagar(force = false): void {
    if (this.pagando && !force) return;

    this.formaMenuAberto = false;
    this.pagarAberto = false;
    this.ticketPagar = null;
    this.formaPagamento = 'PIX';
    this.codigoReferencia = '';
    this.observacaoPagamento = '';
    this.pagarErro = null;
    this.formaMenuAberto = false;

    this.cdr.detectChanges();
  }

  protected confirmarPagamento(): void {
    if (!this.ticketPagar) return;

    const idTicket = this.getId(this.ticketPagar);
    if (!idTicket) {
      this.pagarErro = 'Ticket inválido.';
      return;
    }

    const payload: CriarPagamentoPayload = {
      idTicket,
      formaPagamento: this.formaPagamento,
      codigoReferencia: this.trimOrNull(this.codigoReferencia),
      observacao: this.trimOrNull(this.observacaoPagamento),
    };

    this.pagando = true;
    this.pagarErro = null;
    this.cdr.detectChanges();

    this.pagamentosApi
      .criar(payload)
      .pipe(
        finalize(() => {
          this.pagando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (p: Pagamento) => {
          this.fecharPagar(true);
          this.ticketsComPagamentoEmAberto.add(idTicket);

          this.showToast(
            `Pagamento criado para o ticket ${this.ticketCodigoFromId(Number((p as any)?.idTicket ?? idTicket))}. Status: PENDENTE.`,
            'ok'
          );

          this.carregarPagamentosEmAberto();
          this.carregar();
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          const msg =
            (typeof e.error === 'string' && e.error) ||
            (e.error as any)?.message ||
            'Não foi possível criar o pagamento.';
          this.pagarErro = msg;
        },
      });
  }

  protected toggleFormaMenu(): void {
    if (this.pagando) return;
    this.formaMenuAberto = !this.formaMenuAberto;
  }

  protected closeFormaMenu(): void {
    this.formaMenuAberto = false;
  }

  protected selecionarForma(f: FormaPagamento): void {
    this.formaPagamento = f;
    this.formaMenuAberto = false;
  }

  protected formaPagamentoLabel(f: FormaPagamento): string {
    switch (f) {
      case 'CREDITO':
        return 'Crédito';
      case 'DEBITO':
        return 'Débito';
      case 'PIX':
        return 'PIX';
      case 'DINHEIRO':
        return 'Dinheiro';
      case 'BOLETO':
        return 'Boleto';
      case 'TRANSFERENCIA':
        return 'Transferência';
      default:
        return f;
    }
  }

  private trimOrNull(v: string): string | null {
    const s = String(v ?? '').trim();
    return s ? s : null;
  }

  protected detalhar(t: Ticket): void {
    const id = this.getId(t);
    if (!id) return;

    this.modalTitulo = 'Detalhes do ticket';
    this.modalHtml = `
      <div class="md">
        <div><b>Ticket:</b> ${this.ticketCodigoFromId(id)}</div>
        <div><b>Placa:</b> ${this.escapeHtml(this.placaOf(t))}</div>
        <div><b>Zona:</b> ${this.escapeHtml(this.zonaOf(t))}</div>
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

  protected fecharModal(): void {
    this.modalAberto = false;
    this.modalTitulo = '';
    this.modalHtml = '';
    this.acaoConfirmar = null;
    this.textoConfirmar = 'Confirmar';
  }

  protected voltarHome(): void {
    this.router.navigateByUrl('/home');
  }

  protected irMapa(): void {
    this.router.navigateByUrl('/mapa');
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

  protected podeCancelar(t: Ticket): boolean {
    const s = this.statusOf(t);
    return this.isAdmin() && (s === 'ATIVO' || s === 'RESERVADO');
  }

  protected cancelar(t: Ticket): void {
    const id = this.getId(t);
    if (!id) return;

    this.modalTitulo = 'Cancelar ticket';
    this.modalHtml = `
      <div class="md">
        Deseja cancelar o ticket da placa <b>${this.escapeHtml(this.placaOf(t))}</b>?
        <div class="hint">Essa ação não pode ser desfeita.</div>
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
          error: (e: HttpErrorResponse) => {
            console.error(e);
            const msg =
              (typeof e.error === 'string' && e.error) ||
              (e.error as any)?.message ||
              'Não foi possível cancelar.';
            this.modalHtml = `<div class="md"><div class="err">${this.escapeHtml(msg)}</div></div>`;
            this.acaoConfirmar = null;
            this.textoConfirmar = 'Ok';
          },
        });
    };

    this.modalAberto = true;
  }

  protected toastMsg: string | null = null;
  protected toastVariant: 'ok' | 'warn' | 'err' = 'ok';
  private toastTimer: any = null;

  protected showToast(msg: string, variant: 'ok' | 'warn' | 'err' = 'ok'): void {
    this.toastMsg = msg;
    this.toastVariant = variant;

    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.clearToast(), 3500);

    this.cdr.detectChanges();
  }

  protected clearToast(): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = null;
    this.toastMsg = null;
    this.cdr.detectChanges();
  }
}

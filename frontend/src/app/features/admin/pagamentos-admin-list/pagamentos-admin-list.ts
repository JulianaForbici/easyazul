import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';

import {
  FormaPagamento,
  Pagamento,
  PagamentoService,
  Page,
  StatusPagamento,
} from '../../../core/api/pagamento.service/pagamento';
import { AuthService } from '../../../core/auth/auth.service';

type FiltroStatus = 'TODOS' | 'PENDENTE' | 'APROVADO' | 'RECUSADO' | 'CANCELADO';

@Component({
  standalone: true,
  selector: 'app-pagamentos-admin-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './pagamentos-admin-list.html',
  styleUrl: './pagamentos-admin-list.scss',
})
export class PagamentosAdminListComponent implements OnInit {
  protected pagamentosAll: Pagamento[] = [];
  protected pagamentosFiltrados: Pagamento[] = [];

  protected carregando = false;
  protected erro: string | null = null;

  protected busca = '';
  protected filtroStatus: FiltroStatus = 'TODOS';

  protected page = 0;
  protected size = 10;
  protected pageData: Page<Pagamento> | null = null;

  protected toastMsg: string | null = null;
  protected toastTipo: 'ok' | 'bad' = 'ok';
  private toastTimer: any = null;

  protected modalAberto = false;
  protected modalTitulo = '';
  protected modalHtml = '';
  protected acaoConfirmar: (() => void) | null = null;
  protected textoConfirmar = 'Confirmar';

  protected modalCancelarAberto = false;
  protected pagamentoCancelar: Pagamento | null = null;
  protected motivoCancelamento = '';
  protected erroCancelar: string | null = null;
  protected cancelando = false;

  constructor(
    private api: PagamentoService,
    protected auth: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  protected showToast(msg: string, tipo: 'ok' | 'bad' = 'ok', ms = 3200): void {
    this.toastMsg = msg;
    this.toastTipo = tipo;

    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.clearToast(), ms);

    this.cdr.detectChanges();
  }

  protected clearToast(): void {
    this.toastMsg = null;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = null;
    this.cdr.detectChanges();
  }

  protected carregar(): void {
    this.carregarTudo();
  }

  private carregarTudo(): void {
    this.carregando = true;
    this.erro = null;
    this.cdr.detectChanges();

    this.api
      .listarTodos(null, 0, 5000)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (res) => {
          this.pagamentosAll = res?.content ?? [];
          this.page = 0;
          this.aplicarFiltrosEPaginar();
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          this.pageData = null;
          this.pagamentosAll = [];
          this.pagamentosFiltrados = [];
          this.erro = this.pickMsg(e, 'Não foi possível carregar os pagamentos.');
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

  protected setFiltro(f: FiltroStatus): void {
    this.filtroStatus = f;
    this.page = 0;
    this.aplicarFiltrosEPaginar();
  }

  protected aplicarBusca(): void {
    this.page = 0;
    this.aplicarFiltrosEPaginar();
  }

  private statusKey(s: any): string {
    return String(s ?? '').trim().toUpperCase();
  }

  private isPendenteStatus(s: any): boolean {
    const k = this.statusKey(s);
    return k === 'PENDENTE' || k.startsWith('PENDENTE');
  }

  private aplicarFiltrosEPaginar(): void {
    const q = (this.busca ?? '').trim().toLowerCase();

    const base = !q
      ? this.pagamentosAll
      : this.pagamentosAll.filter((p) => {
          const id = String((p as any)?.idPagamento ?? '').toLowerCase();
          const ticket = String((p as any)?.idTicket ?? '').toLowerCase();
          const status = String((p as any)?.status ?? '').toLowerCase();
          const metodo = String((p as any)?.metodo ?? '').toLowerCase();
          const valor = String((p as any)?.valor ?? '').toLowerCase();
          const obs = String((p as any)?.observacao ?? '').toLowerCase();
          const cod = String((p as any)?.codigoReferencia ?? '').toLowerCase();
          const data = String((p as any)?.dataPagamento ?? '').toLowerCase();

          return (
            id.includes(q) ||
            ticket.includes(q) ||
            status.includes(q) ||
            metodo.includes(q) ||
            valor.includes(q) ||
            obs.includes(q) ||
            cod.includes(q) ||
            data.includes(q)
          );
        });

    const f = this.statusKey(this.filtroStatus);

    const filtrados = base.filter((p) => {
      if (f === 'TODOS') return true;
      if (f === 'PENDENTE') return this.isPendenteStatus((p as any)?.status);
      return this.statusKey((p as any)?.status) === f;
    });

    const total = filtrados.length;
    const totalPages = total === 0 ? 0 : Math.ceil(total / this.size);

    if (totalPages === 0) {
      this.page = 0;
      this.pagamentosFiltrados = [];
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
    this.pagamentosFiltrados = filtrados.slice(start, end);

    this.pageData = {
      content: this.pagamentosFiltrados,
      number: this.page,
      size: this.size,
      totalElements: total,
      totalPages,
      first: this.page === 0,
      last: this.page >= totalPages - 1,
    } as any;

    this.cdr.detectChanges();
  }

  protected get qtdPendentes(): number {
    return (this.pagamentosAll ?? []).filter((p) => this.isPendenteStatus((p as any)?.status)).length;
  }

  protected get qtdAprovados(): number {
    return (this.pagamentosAll ?? []).filter((p) => this.statusKey((p as any)?.status) === 'APROVADO').length;
  }

  protected get qtdRecusados(): number {
    return (this.pagamentosAll ?? []).filter((p) => this.statusKey((p as any)?.status) === 'RECUSADO').length;
  }

  protected get qtdCancelados(): number {
    return (this.pagamentosAll ?? []).filter((p) => this.statusKey((p as any)?.status) === 'CANCELADO').length;
  }

  protected podeConfirmar(p: Pagamento): boolean {
    return this.auth.isFiscalOuAdmin() && this.isPendenteStatus((p as any)?.status);
  }

  protected podeCancelar(p: Pagamento): boolean {
    return this.auth.isAdmin() && this.statusKey((p as any)?.status) !== 'CANCELADO';
  }

  protected confirmar(p: Pagamento): void {
    if (!this.podeConfirmar(p)) return;

    const id = Number((p as any)?.idPagamento ?? 0);
    if (!id) return;

    this.modalTitulo = 'Confirmar pagamento';
    this.modalHtml = `
      <div class="md">
        Confirmar pagamento <b>#${this.escapeHtml(String(id))}</b> do ticket <b>#${this.escapeHtml(this.ticketOf(p))}</b>?
        <div class="hint">O status será alterado para <b>APROVADO</b>.</div>
      </div>
    `;
    this.textoConfirmar = 'Confirmar';

    this.acaoConfirmar = () => {
      this.carregando = true;
      this.erro = null;
      this.cdr.detectChanges();

      this.api
        .confirmar(id)
        .pipe(
          finalize(() => {
            this.carregando = false;
            this.cdr.detectChanges();
          })
        )
        .subscribe({
          next: () => {
            this.fecharModal();
            this.showToast('Ok, pagamento aprovado!', 'ok');
            this.carregar();
          },
          error: (e: HttpErrorResponse) => {
            console.error(e);
            const msg = this.pickMsg(e, 'Não foi possível confirmar o pagamento.');
            this.showToast(msg, 'bad');
          },
        });
    };

    this.modalAberto = true;
  }

  protected abrirCancelar(p: Pagamento, ev?: MouseEvent): void {
    ev?.stopPropagation();
    if (!this.podeCancelar(p)) return;

    this.pagamentoCancelar = p;
    this.motivoCancelamento = '';
    this.erroCancelar = null;
    this.modalCancelarAberto = true;
    this.cdr.detectChanges();
  }

  protected fecharCancelar(force = false): void {
    if (this.cancelando && !force) return;

    this.modalCancelarAberto = false;
    this.pagamentoCancelar = null;
    this.motivoCancelamento = '';
    this.erroCancelar = null;
    this.cdr.detectChanges();
  }

  protected confirmarCancelar(): void {
    if (!this.pagamentoCancelar) return;

    const id = Number((this.pagamentoCancelar as any)?.idPagamento ?? 0);
    if (!id) return;

    const motivo = (this.motivoCancelamento ?? '').trim();
    if (!motivo) {
      this.erroCancelar = 'Motivo é obrigatório para cancelamento pelo Administrador.';
      return;
    }

    this.cancelando = true;
    this.erroCancelar = null;
    this.cdr.detectChanges();

    this.api
      .cancelar(id, motivo)
      .pipe(
        finalize(() => {
          this.cancelando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.fecharCancelar(true);
          this.showToast('Ok, cancelamento feito.', 'ok');
          this.carregar();
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          const msg = this.pickMsg(e, 'Não foi possível cancelar o pagamento.');
          this.erroCancelar = msg;
          this.showToast(msg, 'bad');
        },
      });
  }

  protected detalhar(p: Pagamento): void {
    const id = Number((p as any)?.idPagamento ?? 0);
    this.modalTitulo = 'Detalhes do pagamento';
    this.modalHtml = `
      <div class="md">
        <div><b>ID:</b> #${this.escapeHtml(String(id || '-'))}</div>
        <div><b>Ticket:</b> #${this.escapeHtml(this.ticketOf(p))}</div>
        <div><b>Status:</b> ${this.escapeHtml(this.statusLabel((p as any)?.status))}</div>
        <div><b>Método:</b> ${this.escapeHtml(this.metodoLabel((p as any)?.metodo))}</div>
        <div><b>Valor:</b> ${this.escapeHtml(this.money((p as any)?.valor))}</div>
        <div><b>Data:</b> ${this.escapeHtml(this.dataLabel((p as any)?.dataPagamento))}</div>
        <div><b>Código ref.:</b> ${this.escapeHtml((p as any)?.codigoReferencia || '-')}</div>
        <div><b>Observação:</b> ${this.escapeHtml((p as any)?.observacao || '-')}</div>
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
    this.cdr.detectChanges();
  }

  protected ticketOf(p: Pagamento): string {
    const idTicket = (p as any)?.idTicket;
    return idTicket === null || idTicket === undefined ? '-' : String(idTicket);
  }

  protected metodoLabel(m: FormaPagamento | null | undefined): string {
    const s = this.statusKey(m);
    if (s === 'CREDITO') return 'Crédito';
    if (s === 'DEBITO') return 'Débito';
    if (s === 'PIX') return 'Pix';
    if (s === 'DINHEIRO') return 'Dinheiro';
    if (s === 'BOLETO') return 'Boleto';
    if (s === 'TRANSFERENCIA') return 'Transferência';
    return '-';
  }

  protected statusLabel(s: StatusPagamento | null | undefined): string {
    if (!s) return '-';
    if (this.isPendenteStatus(s)) return 'Pendente';
    if (this.statusKey(s) === 'APROVADO') return 'Aprovado';
    if (this.statusKey(s) === 'RECUSADO') return 'Recusado';
    return 'Cancelado';
  }

  protected statusClass(p: Pagamento): string {
    const k = this.statusKey((p as any)?.status);
    if (k === 'APROVADO') return 'ok';
    if (this.isPendenteStatus((p as any)?.status)) return 'warn';
    if (k === 'RECUSADO') return 'bad';
    return 'muted';
  }

  protected money(v: number | null | undefined): string {
    const n = Number(v ?? 0);
    return n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected dataLabel(iso?: string | null): string {
    if (!iso) return '-';
    const s = String(iso);
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
      const [y, m, d] = s.split('-');
      return `${d}/${m}/${y}`;
    }
    return s;
  }

  protected voltarAdminHome(): void {
    this.router.navigateByUrl('/admin/usuarios');
  }

  protected trackById = (_: number, p: Pagamento) => Number((p as any)?.idPagamento ?? 0);

  private pickMsg(e: HttpErrorResponse, fallback: string): string {
    const raw =
      (typeof e.error === 'string' && e.error) ||
      (e.error as any)?.message ||
      e.message ||
      null;
    return raw ? String(raw) : fallback;
  }

  private escapeHtml(v: string): string {
    return String(v ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }
}

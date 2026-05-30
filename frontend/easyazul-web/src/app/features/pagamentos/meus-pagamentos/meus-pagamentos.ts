import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import {
  CriarPagamentoPayload,
  FormaPagamento,
  Pagamento,
  PagamentoService,
  StatusPagamento,
  Page,
} from '../../../core/api/pagamento.service/pagamento';
import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';

type FiltroStatus = 'TODOS' | 'PENDENTE' | 'APROVADO' | 'RECUSADO' | 'CANCELADO';

@Component({
  standalone: true,
  selector: 'app-meus-pagamentos',
  imports: [CommonModule, FormsModule],
  templateUrl: './meus-pagamentos.html',
  styleUrl: './meus-pagamentos.scss',
})
export class MeusPagamentosComponent implements OnInit, OnDestroy {
  protected pagamentos: Pagamento[] = [];
  private pagamentosAll: Pagamento[] = [];
  protected pagamentosFiltrados: Pagamento[] = [];

  protected carregando = false;
  protected erro: string | null = null;

  protected busca = '';
  protected filtroStatus: FiltroStatus = 'TODOS';

  protected page = 0;
  protected size = 10;
  protected pageData: Page<Pagamento> | null = null;

  protected modalDetalhesAberto = false;
  protected pagamentoSelecionado: Pagamento | null = null;

  protected modalErroAberto = false;
  protected modalErroMsg = '';

  protected modalPagarAberto = false;
  protected pagarTicketId: number | null = null;
  protected pagarMetodo: FormaPagamento = 'PIX';
  protected pagarCodigoReferencia = '';
  protected pagarObservacao = '';
  protected erroPagar: string | null = null;
  protected salvandoPagamento = false;

  protected modalCancelarAberto = false;
  protected pagamentoCancelar: Pagamento | null = null;
  protected motivoCancelamento = '';
  protected erroCancelar: string | null = null;
  protected cancelando = false;

  protected toastMsg: string | null = null;
  protected toastVariant: 'ok' | 'warn' | 'err' = 'ok';
  private toastTimer: any = null;

  constructor(private api: PagamentoService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.carregarTudo();
  }

  ngOnDestroy(): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
  }

  protected get isFirst(): boolean {
    return this.page <= 0;
  }

  protected get isLast(): boolean {
    const totalPages = this.pageData?.totalPages ?? 0;
    return totalPages ? this.page >= totalPages - 1 : true;
  }

  protected prev(): void {
    if (this.page <= 0) return;
    this.page--;
    this.atualizarPagina();
  }

  protected next(): void {
    const totalPages = this.pageData?.totalPages ?? 0;
    if (totalPages && this.page >= totalPages - 1) return;
    this.page++;
    this.atualizarPagina();
  }

  protected setFiltro(f: FiltroStatus): void {
    this.filtroStatus = f;
    this.page = 0;
    this.aplicarFiltros();
    this.atualizarPagina();
  }

  protected aplicarBusca(): void {
    this.page = 0;
    this.aplicarFiltros();
    this.atualizarPagina();
  }

  protected carregarTudo(): void {
    this.carregando = true;
    this.erro = null;
    this.cdr.detectChanges();

    const batchSize = 200;

    const acc: Pagamento[] = [];
    const fetchPage = (p: number) => {
      this.api
        .listarMeus(p, batchSize)
        .pipe(
          finalize(() => {
            this.cdr.detectChanges();
          })
        )
        .subscribe({
          next: (res) => {
            const content = (res?.content ?? []) as Pagamento[];
            acc.push(...content);

            const last = !!(res as any)?.last;
            const totalPages = Number((res as any)?.totalPages ?? 0);

            if (!last && totalPages && p < totalPages - 1) {
              fetchPage(p + 1);
              return;
            }

            this.pagamentosAll = acc;
            this.page = 0;
            this.aplicarFiltros();
            this.atualizarPagina();
            this.carregando = false;
            this.cdr.detectChanges();
          },
          error: (e: HttpErrorResponse) => {
            console.error(e);
            this.pagamentosAll = [];
            this.pagamentos = [];
            this.pagamentosFiltrados = [];
            this.pageData = this.makePage([], 0, this.size, 0);
            this.carregando = false;
            this.erro = 'Não foi possível carregar seus pagamentos.';
            this.abrirModalErro(this.pickMsg(e, this.erro));
            this.cdr.detectChanges();
          },
        });
    };

    fetchPage(0);
  }

  private aplicarFiltros(): void {
    const q = (this.busca ?? '').trim().toLowerCase();

    const base = !q
      ? this.pagamentosAll
      : this.pagamentosAll.filter((p) => {
          const idNumerico = String((p as any)?.idPagamento ?? '').toLowerCase();
          const id = this.pagamentoCodigoFromId((p as any)?.idPagamento).toLowerCase();
          const ticket = `${String((p as any)?.idTicket ?? '')} ${this.ticketCodigoFromId((p as any)?.idTicket)}`.toLowerCase();
          const status = String((p as any)?.status ?? '').toLowerCase();
          const metodo = String((p as any)?.metodo ?? '').toLowerCase();
          const valor = String((p as any)?.valor ?? '').toLowerCase();
          const obs = String((p as any)?.observacao ?? '').toLowerCase();
          const cod = String((p as any)?.codigoReferencia ?? '').toLowerCase();

          return (
            id.includes(q) ||
            idNumerico.includes(q) ||
            ticket.includes(q) ||
            status.includes(q) ||
            metodo.includes(q) ||
            valor.includes(q) ||
            obs.includes(q) ||
            cod.includes(q)
          );
        });

    const f = String(this.filtroStatus ?? 'TODOS').toUpperCase();

    this.pagamentosFiltrados =
      f === 'TODOS'
        ? base
        : base.filter((p) => String((p as any)?.status ?? '').toUpperCase() === f);
  }

  private atualizarPagina(): void {
    const total = this.pagamentosFiltrados.length;
    const totalPages = Math.max(1, Math.ceil(total / this.size));

    if (this.page >= totalPages) this.page = totalPages - 1;
    if (this.page < 0) this.page = 0;

    const start = this.page * this.size;
    const end = start + this.size;

    this.pagamentos = this.pagamentosFiltrados.slice(start, end);
    this.pageData = this.makePage(this.pagamentos, this.page, this.size, total);

    this.cdr.detectChanges();
  }

  private makePage(content: Pagamento[], number: number, size: number, totalElements: number): Page<Pagamento> {
    const totalPages = Math.max(1, Math.ceil(totalElements / size));
    return {
      content,
      number,
      size,
      totalElements,
      totalPages,
      first: number <= 0,
      last: number >= totalPages - 1,
    } as any;
  }

  protected get qtdPendentes(): number {
    return (this.pagamentosAll ?? []).filter((p) => String((p as any)?.status ?? '').toUpperCase() === 'PENDENTE').length;
  }

  protected get qtdAprovados(): number {
    return (this.pagamentosAll ?? []).filter((p) => String((p as any)?.status ?? '').toUpperCase() === 'APROVADO').length;
  }

  protected get qtdRecusados(): number {
    return (this.pagamentosAll ?? []).filter((p) => String((p as any)?.status ?? '').toUpperCase() === 'RECUSADO').length;
  }

  protected get qtdCancelados(): number {
    return (this.pagamentosAll ?? []).filter((p) => String((p as any)?.status ?? '').toUpperCase() === 'CANCELADO').length;
  }

  protected abrirDetalhes(p: Pagamento): void {
    this.pagamentoSelecionado = p;
    this.modalDetalhesAberto = true;
    this.cdr.detectChanges();
  }

  protected fecharDetalhes(): void {
    this.modalDetalhesAberto = false;
    this.pagamentoSelecionado = null;
    this.cdr.detectChanges();
  }

  protected abrirPagar(ticketId: number): void {
    this.erroPagar = null;
    this.pagarTicketId = Number(ticketId);
    this.pagarMetodo = 'PIX';
    this.pagarCodigoReferencia = '';
    this.pagarObservacao = '';
    this.modalPagarAberto = true;
    this.cdr.detectChanges();
  }

  protected fecharPagar(): void {
    if (this.salvandoPagamento) return;
    this.modalPagarAberto = false;
    this.cdr.detectChanges();
  }

  protected confirmarPagamento(): void {
    this.erroPagar = null;

    const idTicket = Number(this.pagarTicketId ?? 0);
    if (!idTicket) {
      this.erroPagar = 'Ticket inválido.';
      this.cdr.detectChanges();
      return;
    }

    const payload: CriarPagamentoPayload = {
      idTicket,
      formaPagamento: this.pagarMetodo,
      codigoReferencia: (this.pagarCodigoReferencia ?? '').trim() || null,
      observacao: (this.pagarObservacao ?? '').trim() || null,
    };

    this.salvandoPagamento = true;
    this.cdr.detectChanges();

    this.api
      .criar(payload)
      .pipe(
        finalize(() => {
          this.salvandoPagamento = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.modalPagarAberto = false;
          this.showToast('Pagamento criado com sucesso. Aguardando confirmação.', 'ok');
          this.carregarTudo();
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          this.erroPagar = this.pickMsg(e, 'Não foi possível criar o pagamento.');
          this.cdr.detectChanges();
        },
      });
  }

  protected podeCancelar(p: Pagamento): boolean {
    return String((p as any)?.status ?? '').toUpperCase() === 'PENDENTE';
  }

  protected abrirCancelar(p: Pagamento, ev?: MouseEvent): void {
    ev?.stopPropagation();
    this.pagamentoCancelar = p;
    this.motivoCancelamento = 'Cancelamento solicitado pelo usuário';
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

    this.cancelando = true;
    this.erroCancelar = null;
    this.cdr.detectChanges();

    const motivoPadrao = 'Cancelamento solicitado pelo usuário';
    const motivo = (this.motivoCancelamento ?? '').trim() || motivoPadrao;

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
          this.showToast(`Pagamento ${this.pagamentoCodigoFromId(id)} cancelado com sucesso.`, 'ok');
          this.carregarTudo();
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          this.erroCancelar = this.pickMsg(e, 'Não foi possível cancelar o pagamento.');
          this.cdr.detectChanges();
        },
      });
  }

  protected abrirModalErro(msg: string): void {
    this.modalErroMsg = msg;
    this.modalErroAberto = true;
    this.cdr.detectChanges();
  }

  protected fecharModalErro(): void {
    this.modalErroAberto = false;
    this.modalErroMsg = '';
    this.cdr.detectChanges();
  }

  protected getId(p: Pagamento): number {
    return Number((p as any)?.idPagamento ?? 0);
  }

  protected getTicket(p: Pagamento): string {
    return this.ticketCodigoFromId((p as any)?.idTicket);
  }

  protected pagamentoCodigo(p: Pagamento): string {
    return this.pagamentoCodigoFromId((p as any)?.idPagamento);
  }

  protected pagamentoCodigoFromId(id: number | null | undefined): string {
    const n = Number(id || 0);
    if (!n) return '—';
    return `PGT-${String(n).padStart(6, '0')}`;
  }

  protected ticketCodigoFromId(id: number | null | undefined): string {
    const n = Number(id || 0);
    if (!n) return '—';
    return `TCK-${String(n).padStart(6, '0')}`;
  }

  protected metodoLabel(m: FormaPagamento | null | undefined): string {
    const s = String(m ?? '').toUpperCase();
    if (s === 'CREDITO') return 'Crédito';
    if (s === 'DEBITO') return 'Débito';
    if (s === 'PIX') return 'Pix';
    if (s === 'DINHEIRO') return 'Dinheiro';
    if (s === 'BOLETO') return 'Boleto';
    if (s === 'TRANSFERENCIA') return 'Transferência';
    return '-';
  }

  protected formatMoney(v: number): string {
    const n = Number(v ?? 0);
    return n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  protected formatDate(iso?: string | null): string {
    if (!iso) return '-';
    const s = String(iso);
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
      const [y, m, d] = s.split('-');
      return `${d}/${m}/${y}`;
    }
    return s;
  }

  protected statusLabel(s: StatusPagamento): string {
    const k = String(s ?? '').toUpperCase();
    if (k === 'PENDENTE') return 'Pendente';
    if (k === 'APROVADO') return 'Aprovado';
    if (k === 'RECUSADO') return 'Recusado';
    return 'Cancelado';
  }

  protected trackById = (_: number, p: Pagamento) => this.getId(p);

  private pickMsg(e: HttpErrorResponse, fallback: string): string {
    const raw = (typeof e.error === 'string' && e.error) || e.error?.message || e.message || null;
    return raw ? String(raw) : fallback;
  }

  protected showToast(msg: string, variant: 'ok' | 'warn' | 'err' = 'ok'): void {
    this.toastMsg = msg;
    this.toastVariant = variant;

    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => {
      this.toastMsg = null;
      this.cdr.detectChanges();
    }, 3500);

    this.cdr.detectChanges();
  }

  protected clearToast(): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastMsg = null;
    this.cdr.detectChanges();
  }
}

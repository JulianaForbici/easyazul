import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type StatusPagamento = 'PENDENTE' | 'APROVADO' | 'RECUSADO' | 'CANCELADO';
export type FormaPagamento = 'CREDITO' | 'DEBITO' | 'PIX' | 'DINHEIRO' | 'BOLETO' | 'TRANSFERENCIA';

export type Pagamento = {
  idPagamento: number;
  idTicket: number | null;
  valor: number;
  metodo: FormaPagamento;
  status: StatusPagamento;
  observacao?: string | null;
  dataPagamento?: string | null;
  codigoReferencia?: string | null;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
};

export type CriarPagamentoPayload = {
  idTicket: number;
  formaPagamento: FormaPagamento;
  codigoReferencia?: string | null;
  observacao?: string | null;
};

@Injectable({ providedIn: 'root' })
export class PagamentoService {
  private base = `${environment.apiUrl}/pagamentos`;

  constructor(private http: HttpClient) {}

  listarMeus(page = 0, size = 10): Observable<Page<Pagamento>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'idPagamento,desc');

    return this.http.get<Page<Pagamento>>(`${this.base}/meus`, { params });
  }

  listarTodos(ticketId: number | null, page = 0, size = 10): Observable<Page<Pagamento>> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'idPagamento,desc');

    if (ticketId !== null && ticketId !== undefined) {
      params = params.set('ticketId', String(ticketId));
    }

    return this.http.get<Page<Pagamento>>(this.base, { params });
  }

  criar(payload: CriarPagamentoPayload): Observable<Pagamento> {
    return this.http.post<Pagamento>(this.base, payload);
  }

  confirmar(idPagamento: number): Observable<Pagamento> {
    return this.http.post<Pagamento>(`${this.base}/${idPagamento}/confirmar`, {});
  }

  cancelar(idPagamento: number, motivo: string): Observable<Pagamento> {
    return this.http.post<Pagamento>(`${this.base}/${idPagamento}/cancelar`, { motivo });
  }
}

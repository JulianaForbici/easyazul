import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type StatusTicket = 'ATIVO' | 'FECHADO' | 'CANCELADO' | 'RESERVADO';

export type Ticket = {
  id?: number;
  idTicket?: number;

  idVeiculo?: number;
  placa?: string;

  idDono?: number;
  nomeDono?: string;

  idZona?: number;
  nomeZona?: string;

  inicioTicket?: string;
  venceEm?: string;
  fimTicket?: string;

  valor?: number;
  ativo?: boolean;

  status?: string;
};

export type Page<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
};

export type DadosRenovacaoTicket = {
  minutosAdicionais: number;
};

@Injectable({ providedIn: 'root' })
export class TicketService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarMeus(page = 0, size = 10): Observable<Page<Ticket>> {
    return this.http.get<Page<Ticket>>(
      `${this.base}/tickets/meus?page=${page}&size=${size}&sort=inicioTicket,desc`
    );
  }

  listarTodos(page = 0, size = 10): Observable<Page<Ticket>> {
    return this.http.get<Page<Ticket>>(
      `${this.base}/tickets?page=${page}&size=${size}&sort=inicioTicket,desc`
    );
  }

  detalhar(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.base}/tickets/${id}`);
  }

  iniciar(id: number): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/iniciar`, {});
  }

  fechar(id: number): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/fechar`, {});
  }

  renovar(id: number): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/renovar`, {});
  }

  cancelar(id: number): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/cancelar`, {});
  }

  renovar(id: number, minutosAdicionais: number): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/renovar`, {
      minutosAdicionais
    });
  }
  
}

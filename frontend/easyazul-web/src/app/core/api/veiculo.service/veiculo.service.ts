import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

export type Veiculo = {
  id?: number;
  idVeiculo?: number;
  placa: string;
  tipoVeiculo: 'CARRO' | 'MOTO' | 'CAMINHAO' | 'ONIBUS' | 'OUTRO';
  status?: string;
  idDono?: number;
};

export type Page<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  number?: number;
  size?: number;
  first?: boolean;
  last?: boolean;
};

@Injectable({ providedIn: 'root' })
export class VeiculoService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarMeus(page = 0, size = 10): Observable<Page<Veiculo>> {
    return this.http.get<Page<Veiculo>>(`${this.base}/veiculos/meus?page=${page}&size=${size}`);
  }

  criarMeu(payload: { placa: string; tipoVeiculo: string; idDono?: number }): Observable<any> {
    return this.http.post(`${this.base}/veiculos`, payload);
  }

  atualizarMeu(id: number, payload: { placa: string; tipoVeiculo: string }): Observable<any> {
    return this.http.put(`${this.base}/veiculos/${id}`, payload);
  }

  excluirMeu(id: number): Observable<any> {
    return this.http.delete(`${this.base}/veiculos/${id}`);
  }

  listarAdmin(
    page = 0,
    size = 50,
    q: string | null = null,
    status: 'ATIVO' | 'INATIVO' | null = null
  ): Observable<Page<Veiculo>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size));
    if (q) params = params.set('q', q);
    if (status) params = params.set('status', status);

    return this.http.get<Page<Veiculo>>(`${this.base}/veiculos`, { params });
  }

  excluirAdmin(id: number): Observable<any> {
    return this.http.delete(`${this.base}/veiculos/${id}`);
  }
}

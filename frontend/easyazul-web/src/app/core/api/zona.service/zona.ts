import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../../environments/environment';

export type StatusZona = 'ATIVA' | 'INATIVA';

export type Zona = {
  id?: number;
  idZona?: number;
  nome: string;
  tarifa: number;
  descricao?: string | null;
  tempoMaximo: number;
  horaInicio: string;
  horaFim: string;
  latitude?: number | null;
  longitude?: number | null;
  capacidadeVagas?: number | null;
  statusZona?: StatusZona | null;
  endereco?: string;
};

export type ZonaUpdate = {
  nome?: string | null;
  tarifa?: number | null;
  descricao?: string | null;
  tempoMaximo?: number | null;
  horaInicio?: string | null;
  horaFim?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  capacidadeVagas?: number | null;
  endereco?: string | null;
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

@Injectable({ providedIn: 'root' })
export class ZonaService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  criar(body: ZonaUpdate): Observable<Zona> {
    return this.http.post<Zona>(`${this.base}/zonas`, body);
  }

  listarAdmin(page = 0, size = 10): Observable<Page<Zona>> {
    return this.http.get<Page<Zona>>(`${this.base}/zonas/admin?page=${page}&size=${size}&sort=nome`);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/zonas/${id}`);
  }

  detalhar(id: number): Observable<Zona> {
    return this.http.get<Zona>(`${this.base}/zonas/${id}`);
  }

  atualizar(id: number, body: ZonaUpdate): Observable<Zona> {
    return this.http.put<Zona>(`${this.base}/zonas/${id}`, body);
  }
}

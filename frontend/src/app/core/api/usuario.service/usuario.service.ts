import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type TipoUsuario = 'MOTORISTA' | 'EMPRESA' | 'ADMINISTRADOR' | 'FISCAL';

export type DadosCadastroUsuario = {
  nome: string;
  email: string;
  senha: string;
  cpf: string | null;
  cnpj: string | null;
  telefone: string;
  tipo: TipoUsuario;
  dataNascimento: string | null;
  razaoSocial: string | null;
};

export type DadosAtualizacaoUsuario = {
  nome: string;
  senha: string | null;
  email: string;
  telefone: string;
  cnpj: string | null;
  cpf: string | null;
};

export type UsuarioDetalhe = {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  tipo: TipoUsuario;
  status: string;
  dataNascimento: string | null;
  razaoSocial: string | null;
  cpf?: string | null;
  cnpj?: string | null;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private base = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  cadastrar(dados: DadosCadastroUsuario): Observable<UsuarioDetalhe> {
    return this.http.post<UsuarioDetalhe>(this.base, dados);
  }

  cadastrarAdmin(dados: DadosCadastroUsuario): Observable<UsuarioDetalhe> {
    return this.http.post<UsuarioDetalhe>(`${this.base}/admin`, dados);
  }

  listar(page = 0, size = 10, sort = 'nome'): Observable<Page<UsuarioDetalhe>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<UsuarioDetalhe>>(this.base, { params });
  }

  detalhar(id: number): Observable<UsuarioDetalhe> {
    return this.http.get<UsuarioDetalhe>(`${this.base}/${id}`);
  }

  atualizar(id: number, dados: DadosAtualizacaoUsuario): Observable<UsuarioDetalhe> {
    return this.http.put<UsuarioDetalhe>(`${this.base}/${id}`, dados);
  }

  inativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  listarAdmin(page = 0, size = 10, sort = 'nome'): Observable<Page<UsuarioDetalhe>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', sort);

    return this.http.get<Page<UsuarioDetalhe>>(this.base, { params });
  }

  reativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/reativar`, {});
  }
}

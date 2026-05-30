import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';

import { VeiculoService, Veiculo, Page } from '../../../core/api/veiculo.service/veiculo.service';

type TipoVeiculo = 'CARRO' | 'MOTO' | 'CAMINHAO' | 'ONIBUS' | 'OUTRO';
type FiltroStatus = 'TODOS' | 'ATIVOS' | 'INATIVOS';

@Component({
  selector: 'app-veiculos-admin-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './veiculos-admin-list.html',
  styleUrl: './veiculos-admin-list.scss',
})
export class VeiculosAdminListComponent implements OnInit {
  protected veiculos: Veiculo[] = [];
  protected carregando = false;
  protected erro: string | null = null;

  protected busca = '';
  protected filtroStatus: FiltroStatus = 'TODOS';

  protected page = 0;
  protected size = 10;
  protected pageData: Page<Veiculo> | null = null;

  protected confirmarExcluirAberto = false;
  protected veiculoParaExcluir: any | null = null;
  protected erroExcluir: string | null = null;

  constructor(private api: VeiculoService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.carregar();
  }

  protected get isFirst(): boolean {
    return !!this.pageData?.first;
  }

  protected get isLast(): boolean {
    return !!this.pageData?.last;
  }

  protected setFiltro(f: FiltroStatus): void {
    this.filtroStatus = f;
    this.page = 0;
    this.carregar();
  }

  protected aplicarBusca(): void {
    this.page = 0;
    this.carregar();
  }

  protected carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.cdr.detectChanges();

    const q = (this.busca ?? '').trim() || null;
    const status =
      this.filtroStatus === 'TODOS'
        ? null
        : this.filtroStatus === 'ATIVOS'
          ? 'ATIVO'
          : 'INATIVO';

    this.api
      .listarAdmin(this.page, this.size, q, status)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (res: Page<Veiculo>) => {
          this.pageData = res;
          this.veiculos = res?.content ?? [];
        },
        error: (err: unknown) => {
          console.error(err);
          this.pageData = null;
          this.veiculos = [];
          this.erro = 'Não foi possível carregar a listagem de veículos.';
        },
      });
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

  protected getId(v: any): number {
    return Number(v?.idVeiculo ?? v?.id ?? 0);
  }

  protected placaOf(v: any): string {
    return String(v?.placa ?? '').toUpperCase();
  }

  protected donoId(v: any): string {
    const id = v?.idDono ?? v?.idUsuarioDono ?? v?.donoId ?? null;
    return id === null || id === undefined || id === '' ? '—' : String(id);
  }

  protected tipoOf(v: any): TipoVeiculo {
    const t = String(v?.tipoVeiculo ?? 'OUTRO').toUpperCase() as TipoVeiculo;
    if (t === 'CARRO' || t === 'MOTO' || t === 'CAMINHAO' || t === 'ONIBUS') return t;
    return 'OUTRO';
  }

  protected tipoLabel(t: TipoVeiculo | string | undefined): string {
    switch (t) {
      case 'CARRO': return 'Carro';
      case 'MOTO': return 'Moto';
      case 'CAMINHAO': return 'Caminhão';
      case 'ONIBUS': return 'Ônibus';
      default: return 'Outro';
    }
  }

  protected tipoLabelOf(v: any): string {
    return this.tipoLabel(this.tipoOf(v));
  }

  protected tipoClassOf(v: any): 'carro' | 'moto' | 'caminhao' | 'onibus' | 'outro' {
    const t = this.tipoOf(v);
    if (t === 'CARRO') return 'carro';
    if (t === 'MOTO') return 'moto';
    if (t === 'CAMINHAO') return 'caminhao';
    if (t === 'ONIBUS') return 'onibus';
    return 'outro';
  }

  protected statusVeiculo(v: any): 'ATIVO' | 'INATIVO' {
    const raw = v?.statusVeiculo ?? v?.status ?? v?.ativo ?? null;
    const s = String(raw ?? '').toUpperCase();
    if (s === 'INATIVO') return 'INATIVO';
    if (s === 'FALSE') return 'INATIVO';
    return 'ATIVO';
  }

  protected isInativo(v: any): boolean {
    return this.statusVeiculo(v) === 'INATIVO';
  }

  protected statusLabel(v: any): string {
    return this.isInativo(v) ? 'Inativo' : 'Ativo';
  }

  protected statusPillClass(v: any): 'ok' | 'bad' {
    return this.isInativo(v) ? 'bad' : 'ok';
  }

  protected abrirConfirmarExcluir(v: any, ev?: MouseEvent): void {
    ev?.stopPropagation();
    this.veiculoParaExcluir = v;
    this.erroExcluir = null;
    this.confirmarExcluirAberto = true;
  }

  protected fecharConfirmarExcluir(): void {
    this.confirmarExcluirAberto = false;
    this.veiculoParaExcluir = null;
    this.erroExcluir = null;
  }

  protected get placaExcluir(): string {
    return this.placaOf(this.veiculoParaExcluir);
  }

  protected confirmarExcluir(): void {
    if (!this.veiculoParaExcluir) return;

    const id = this.getId(this.veiculoParaExcluir);
    if (!id) {
      this.erroExcluir = 'ID do veículo inválido.';
      return;
    }

    this.carregando = true;
    this.erro = null;
    this.erroExcluir = null;
    this.cdr.detectChanges();

    this.api
      .excluirAdmin(id)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.fecharConfirmarExcluir();

          const totalAtual = Number(this.pageData?.totalElements ?? 0);
          const ficouVazia = this.veiculos.length <= 1;
          if (ficouVazia && this.page > 0 && totalAtual > 0) this.page--;

          this.carregar();
        },
        error: (e: HttpErrorResponse) => {
          console.error(e);
          const msg =
            (typeof e.error === 'string' && e.error) ||
            e.error?.message ||
            'Não foi possível excluir o veículo.';
          this.erroExcluir = msg;
        },
      });
  }

  protected trackById = (_: number, v: any) => this.getId(v) || this.placaOf(v);
}

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs/operators';

import { VeiculoService, Veiculo, Page } from '../../../core/api/veiculo.service/veiculo.service';
import { VeiculosFormComponent } from '../veiculos-form/veiculos-form';
import { AuthService } from '../../../core/auth/auth.service';

type TipoVeiculo = 'CARRO' | 'MOTO' | 'CAMINHAO' | 'ONIBUS' | 'OUTRO';

@Component({
  selector: 'app-meus-veiculos',
  standalone: true,
  imports: [CommonModule, VeiculosFormComponent],
  templateUrl: './meus-veiculos.html',
  styleUrl: './meus-veiculos.scss',
})
export class MeusVeiculosComponent implements OnInit {
  protected easyParkImg = 'assets/images/easy-park.png';

  protected veiculos: Veiculo[] = [];
  protected carregando = false;
  protected erro: string | null = null;

  protected page = 0;
  protected size = 10;
  protected totalPages = 0;
  protected totalElements = 0;

  protected aberto = false;
  protected veiculoEdit: Veiculo | null = null;

  protected idUsuarioLogado = 0;

  // modal confirmação excluir (sem alert)
  protected confirmarExcluirAberto = false;
  protected veiculoParaExcluir: Veiculo | null = null;

  constructor(
    private api: VeiculoService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.idUsuarioLogado = this.auth.getIdUsuario();

    if (!this.idUsuarioLogado) {
      this.erro = 'Não foi possível identificar o usuário logado. Faça login novamente.';
      this.carregando = false;
      return;
    }

    this.carregar();
  }

  protected carregar(): void {
    this.carregando = true;
    this.erro = null;
    this.cdr.detectChanges();

    this.api
      .listarMeus(this.page, this.size)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (res: Page<Veiculo>) => {
          this.veiculos = res?.content ?? [];
          this.totalPages = res?.totalPages ?? 0;
          this.totalElements = res?.totalElements ?? 0;
        },
        error: (err) => {
          console.error(err);
          this.erro = 'Não foi possível carregar seus veículos.';
        },
      });
  }

  protected abrirNovo(): void {
    this.veiculoEdit = null;
    this.aberto = true;
  }

  protected abrirEditar(v: Veiculo): void {
    this.veiculoEdit = v;
    this.aberto = true;
  }

  protected fecharModal(): void {
    this.aberto = false;
  }

  protected atualizado(): void {
    this.aberto = false;
    this.carregar();
  }

  // ===== excluir com modal =====
  protected abrirConfirmarExcluir(v: Veiculo, ev: MouseEvent): void {
    ev.stopPropagation();
    this.veiculoParaExcluir = v;
    this.confirmarExcluirAberto = true;
  }

  protected fecharConfirmarExcluir(): void {
    this.confirmarExcluirAberto = false;
    this.veiculoParaExcluir = null;
  }

  protected confirmarExcluir(): void {
    const v = this.veiculoParaExcluir;
    if (!v) return;

    const id = this.getId(v);
    if (!id) return;

    this.carregando = true;
    this.cdr.detectChanges();

    this.api
      .excluirMeu(id)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.fecharConfirmarExcluir();
          this.carregar();
        },
        error: (err) => {
          console.error(err);
          this.erro = 'Não foi possível excluir o veículo.';
          this.fecharConfirmarExcluir();
        },
      });
  }

  protected getId(v: Veiculo): number {
    return Number((v as any)?.idVeiculo ?? (v as any)?.id ?? 0);
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

  protected badgeTipo(t: TipoVeiculo | string | undefined): 'carro' | 'moto' | 'caminhao' | 'onibus' | 'outro' {
    if (t === 'CARRO') return 'carro';
    if (t === 'MOTO') return 'moto';
    if (t === 'CAMINHAO') return 'caminhao';
    if (t === 'ONIBUS') return 'onibus';
    return 'outro';
  }

  protected prev(): void {
    if (this.page <= 0) return;
    this.page--;
    this.carregar();
  }

  protected next(): void {
    if (this.totalPages && this.page >= this.totalPages - 1) return;
    this.page++;
    this.carregar();
  }
  protected placaOf(v: Veiculo): string {
    return String((v as any)?.placa ?? '').toUpperCase();
  }

  protected tipoOf(v: Veiculo): string {
    return String((v as any)?.tipoVeiculo ?? 'OUTRO').toUpperCase();
  }

  protected get placaExcluir(): string {
    if (!this.veiculoParaExcluir) return '';
    return this.placaOf(this.veiculoParaExcluir);
  }
  protected trackById = (_: number, v: Veiculo) => this.getId(v) || (v as any)?.placa || _;
}

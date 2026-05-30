import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VeiculoService, Veiculo } from '../../../core/api/veiculo.service/veiculo.service';

type TipoVeiculo = 'CARRO' | 'MOTO' | 'CAMINHAO' | 'ONIBUS' | 'OUTRO';
type Modo = 'criar' | 'editar';

@Component({
  selector: 'app-veiculos-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './veiculos-form.html',
  styleUrl: './veiculos-form.scss',
})
export class VeiculosFormComponent implements OnChanges {
  @Input({ required: true }) aberto = false;
  @Input() veiculo: Veiculo | null = null;
  @Input() idDono: number = 0;
  @Input() modoAdmin: boolean = false;
  @Output() fechar = new EventEmitter<void>();
  @Output() atualizado = new EventEmitter<void>();

  modo: Modo = 'criar';
  placa = '';
  tipos: TipoVeiculo[] = ['CARRO', 'MOTO', 'CAMINHAO', 'ONIBUS', 'OUTRO'];
  tipoVeiculo: TipoVeiculo = 'CARRO';

  carregando = false;

  confirmarExcluir = false;
  erro: string | null = null;

  constructor(private api: VeiculoService) {}

  ngOnChanges(): void {
    if (!this.aberto) return;

    this.erro = null;
    this.confirmarExcluir = false;

    if (this.veiculo) {
      this.modo = 'editar';
      this.placa = (this.veiculo as any)?.placa ?? '';
      this.tipoVeiculo = ((this.veiculo as any)?.tipoVeiculo ?? 'CARRO') as TipoVeiculo;
    } else {
      this.modo = 'criar';
      this.placa = '';
      this.tipoVeiculo = 'CARRO';
    }
  }

  onFechar(): void {
    if (this.carregando) return;
    this.confirmarExcluir = false;
    this.erro = null;
    this.fechar.emit();
  }

  salvar(): void {
    this.erro = null;

    const placaNorm = (this.placa ?? '').trim().toUpperCase();
    if (placaNorm.length < 6) {
      this.erro = 'Informe uma placa válida.';
      return;
    }

    const idDonoFinal = Number(this.idDono ?? 0);
    if (!idDonoFinal && this.modo === 'criar' && !this.modoAdmin) {
      this.erro = 'Não foi possível identificar o usuário logado.';
      return;
    }

    this.carregando = true;

    if (this.modo === 'criar') {
      this.api.criarMeu({
        placa: placaNorm,
        tipoVeiculo: this.tipoVeiculo,
        idDono: idDonoFinal || undefined,
      }).subscribe({
        next: () => {
          this.carregando = false;
          this.atualizado.emit();
        },
        error: (err) => {
          console.error(err);
          this.carregando = false;
          this.erro = 'Não foi possível cadastrar o veículo.';
        }
      });
      return;
    }

    const id = Number((this.veiculo as any)?.idVeiculo ?? (this.veiculo as any)?.id ?? 0);
    if (!id) {
      this.carregando = false;
      this.erro = 'Veículo inválido (sem id).';
      return;
    }

    this.api.atualizarMeu(id, {
      placa: placaNorm,
      tipoVeiculo: this.tipoVeiculo,
    }).subscribe({
      next: () => {
        this.carregando = false;
        this.atualizado.emit();
      },
      error: (err) => {
        console.error(err);
        this.carregando = false;
        this.erro = 'Não foi possível atualizar o veículo.'; }});
  }

  abrirConfirmacaoExcluir(): void {
    if (this.carregando) return;
    this.confirmarExcluir = true;
  }

  cancelarExcluir(): void {
    if (this.carregando) return;
    this.confirmarExcluir = false;
  }

  excluir(): void {
    if (!this.veiculo) return;

    const id = Number((this.veiculo as any)?.idVeiculo ?? (this.veiculo as any)?.id ?? 0);
    if (!id) {
      this.erro = 'Veículo inválido (sem id).';
      return;
    }

    this.carregando = true;

    this.api.excluirMeu(id).subscribe({
      next: () => {
        this.carregando = false;
        this.confirmarExcluir = false;
        this.atualizado.emit();
      },
      error: (err) => {
        console.error(err);
        this.carregando = false;
        this.erro = 'Não foi possível excluir o veículo.';
     }});
  }
}

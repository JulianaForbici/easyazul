import { ChangeDetectorRef, Component, Input, OnChanges, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Ticket } from '../../../core/api/ticket.service/ticket';

type EstadoTempo = 'normal' | 'atencao' | 'critico' | 'expirado' | 'indisponivel';

@Component({
  selector: 'app-ticket-time-indicator',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ticket-time-indicator.html',
  styleUrl: './ticket-time-indicator.scss',
})
export class TicketTimeIndicatorComponent implements OnInit, OnChanges, OnDestroy {
  @Input() ticket: Ticket | null = null;

  private agoraMs = Date.now();
  private timer: ReturnType<typeof setInterval> | null = null;

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.atualizarAgora();
    this.timer = setInterval(() => {
      this.atualizarAgora();
      this.cdr.detectChanges();
    }, 1000);
  }

  ngOnChanges(): void {
    this.atualizarAgora();
  }

  ngOnDestroy(): void {
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  protected get possuiPeriodoValido(): boolean {
    const inicio = this.inicioMs;
    const vencimento = this.vencimentoMs;
    return inicio !== null && vencimento !== null && vencimento >= inicio;
  }

  protected get horarioLimite(): string {
    const vencimento = this.vencimentoMs;
    if (vencimento === null) return '—';

    return new Date(vencimento).toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  protected get tempoRestante(): string {
    if (!this.possuiPeriodoValido) return '—';

    const restante = this.vencimentoMs! - this.agoraMs;
    const minutos = restante > 0 ? Math.ceil(restante / 60000) : 0;
    return `${minutos} min`;
  }

  protected get progressoRestante(): number {
    if (!this.possuiPeriodoValido) return 0;

    const duracaoTotal = this.vencimentoMs! - this.inicioMs!;
    if (duracaoTotal <= 0) return this.estadoTempo === 'expirado' ? 0 : 100;

    const restante = this.vencimentoMs! - this.agoraMs;
    return Math.min(100, Math.max(0, (restante / duracaoTotal) * 100));
  }

  protected get estadoTempo(): EstadoTempo {
    if (!this.possuiPeriodoValido) return 'indisponivel';

    const restante = this.vencimentoMs! - this.agoraMs;
    if (restante <= 0) return 'expirado';
    if (restante <= 10 * 60 * 1000) return 'critico';
    if (restante <= 15 * 60 * 1000) return 'atencao';
    return 'normal';
  }

  protected get rotuloEstado(): string {
    switch (this.estadoTempo) {
      case 'atencao':
        return 'Atenção';
      case 'critico':
        return 'Crítico';
      case 'expirado':
        return 'Expirado';
      case 'normal':
        return 'Ativo';
      default:
        return 'Indisponível';
    }
  }

  protected get exibirEstado(): boolean {
    return this.estadoTempo !== 'normal' && this.estadoTempo !== 'indisponivel';
  }

  private get inicioMs(): number | null {
    return this.converterData(this.ticket?.inicioTicket);
  }

  private get vencimentoMs(): number | null {
    return this.converterData(this.ticket?.venceEm);
  }

  private atualizarAgora(): void {
    this.agoraMs = Date.now();
  }

  private converterData(valor: string | undefined): number | null {
    if (!valor) return null;

    const data = new Date(valor).getTime();
    return Number.isFinite(data) ? data : null;
  }
}

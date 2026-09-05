import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { Ticket, TicketService } from '../../../core/api/ticket.service/ticket';
import { AuthService } from '../../../core/auth/auth.service';

type AvisoExpiracao = {
  chave: string;
  ticketId: number;
  placa: string;
  minutos: 10 | 15;
};

@Component({
  selector: 'app-ticket-expiry-alert',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ticket-expiry-alert.html',
  styleUrl: './ticket-expiry-alert.scss',
})
export class TicketExpiryAlertComponent implements OnInit, OnDestroy {

  protected avisos: AvisoExpiracao[] = [];

  private timer: ReturnType<typeof setInterval> | null = null;

  constructor(
    private ticketService: TicketService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.verificarTickets();

    this.timer = setInterval(() => {
      this.verificarTickets();
    }, 30000);
  }

  ngOnDestroy(): void {
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  private verificarTickets(): void {
    if (!this.auth.isAuthenticated() || !this.auth.isUser()) {
      return;
    }

    this.ticketService.listarMeus(0, 5000).subscribe({
      next: (res) => {
        const tickets = res?.content ?? [];

        for (const ticket of tickets) {
          this.verificarTicket(ticket);
        }
      },
      error: (err) => {
        console.error('Erro ao verificar expiração dos tickets', err);
      },
    });
  }

  private verificarTicket(ticket: Ticket): void {
    if (String(ticket.status ?? '').toUpperCase() !== 'ATIVO') {
      return;
    }

    if (!ticket.venceEm) {
      return;
    }

    const vencimento = new Date(ticket.venceEm).getTime();

    if (!Number.isFinite(vencimento)) {
      return;
    }

    const restante = vencimento - Date.now();

    if (restante <= 0) {
      return;
    }

    const minutosRestantes = Math.ceil(restante / 60000);

    if (minutosRestantes <= 10) {
      this.adicionarAviso(ticket, 10);
      return;
    }

    if (minutosRestantes <= 15) {
      this.adicionarAviso(ticket, 15);
    }
  }

  private adicionarAviso(ticket: Ticket, minutos: 10 | 15): void {
    const ticketId = Number(ticket.id ?? ticket.idTicket ?? 0);

    if (!ticketId || !ticket.venceEm) {
      return;
    }

    const chave = `${ticketId}-${ticket.venceEm}-${minutos}`;

    const storageKey = `easyazul:ticket-expiry-alert:${chave}`;

    if (sessionStorage.getItem(storageKey)) {
      return;
    }

    sessionStorage.setItem(storageKey, '1');

    this.avisos.push({
      chave,
      ticketId,
      placa: String(ticket.placa ?? '—').toUpperCase(),
      minutos,
    });

    this.cdr.detectChanges();

    setTimeout(() => {
      this.fechar(chave);
    }, 10000);
  }

  protected fechar(chave: string): void {
    this.avisos = this.avisos.filter((aviso) => aviso.chave !== chave);
    this.cdr.detectChanges();
  }
}

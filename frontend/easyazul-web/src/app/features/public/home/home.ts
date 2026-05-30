import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

type Feature = { title: string; desc: string; img: string };

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomeComponent {
  heroVideo = 'assets/videos/car.mp4';

  chevrons = Array.from({ length: 14 });

  // imagens
  logoBlue = 'assets/images/e-blue.png';
  easyPark = 'assets/images/easy-park.png';
  carArt = 'assets/images/car-e.png';

  // mapa
  mapaBrasil = 'assets/images/mapa-brasil-zona-azul.png';

  features: Feature[] = [
    {
      title: 'Zonas e pins em Blumenau',
      desc: 'Veja zonas disponíveis/ocupadas e detalhes da área (Ex: Centro e Vila Germânica).',
      img: 'assets/images/e-yelllow.png',
    },
    {
      title: 'Ticket com regras da zona',
      desc: 'Fluxo correto: abrir → fechar → pagar. Validação por horário e tempo máximo.',
      img: 'assets/images/ticket.png',
    },
    {
      title: 'Pagamento seguro',
      desc: 'Controle e histórico. Sem auto-confirmação de pagamento pelo usuário.',
      img: 'assets/images/payment.png',
    },
    {
      title: 'Fiscalização & auditoria',
      desc: 'Perfis Fiscal/Admin com permissões e rastreio. JWT + roles ponta a ponta.',
      img: 'assets/images/fiscalizacao.png',
    },
  ];
}

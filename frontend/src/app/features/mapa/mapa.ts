import { AfterViewInit, Component, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import * as L from 'leaflet';
import 'leaflet.markercluster';

import { environment } from '../../../environments/environment';
import { finalize } from 'rxjs/operators';

type ZonaMapa = {
  idZona?: number;
  idZonaEstacionamento?: number;
  id?: number;
  nome?: string;

  latitude: number | string | null;
  longitude: number | string | null;

  capacidadeVagas: number | string | null;
  vagasOcupadas: number | string | null;
  vagasDisponiveis: number | string | null;
};

type ZonaNormalizada = {
  idZona: number;
  nome: string;
  latitude: number;
  longitude: number;
  capacidadeVagas: number;
  vagasOcupadas: number;
  vagasDisponiveis: number;
};

type AuthStore = { idUsuario?: number; token?: string; nome?: string; tipo?: string };

type Veiculo = {
  id?: number;
  idVeiculo?: number;
  placa?: string;
  tipoVeiculo?: string;
  status?: string;
};

type VeiculoTela = {
  id: number;
  placa: string;
  tipoVeiculo: string;
  status?: string;
};

type TicketRecente = {
  idVeiculo?: number;
  placa?: string;
  inicioTicket?: string;
};

@Component({
  selector: 'app-mapa',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mapa.html',
  styleUrl: './mapa.scss',
})
export class MapaComponent implements AfterViewInit {
  private map!: L.Map;
  private cluster!: L.MarkerClusterGroup;

  protected zonaSelecionada: ZonaNormalizada | null = null;

  protected zonas: ZonaNormalizada[] = [];
  protected zonasProximas: Array<ZonaNormalizada & { distanciaKm: number }> = [];

  protected carregandoZonas = false;
  protected erroZonas: string | null = null;

  protected fullscreen = false;

  private minhaPosicao: { lat: number; lng: number; accuracy?: number } | null = null;

  private userMarker: L.Marker | null = null;
  private userAccuracyCircle: L.Circle | null = null;

  protected mostrarTodasZonas = false;

  protected veiculoAtualId: number | null = null;
  protected veiculoAtualPlaca: string | null = null;
  protected veiculoAtualEhRecente = false;
  protected veiculoRecenteId: number | null = null;

  private veiculoAlteradoManualmente = false;

  protected modalVeiculoAberto = false;
  protected carregandoVeiculos = false;
  protected erroVeiculos: string | null = null;

  protected veiculos: VeiculoTela[] = [];
  protected veiculoSelecionadoId: number | null = null;

  protected modalMsgAberto = false;
  protected modalMsgTipo: 'ok' | 'bad' = 'ok';
  protected modalMsgTitulo = '';
  protected modalMsgTexto = '';

  private reservaPendenteZonaId: number | null = null;

  constructor(
    private http: HttpClient,
    private router: Router,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  // ===== storage keys por usuario =====
  private keyVeiculoId(): string {
    const idUsuario = this.getIdUsuarioAuth();
    return idUsuario ? `easyazul:veiculoAtualId:${idUsuario}` : 'idVeiculoAtual';
  }

  private keyVeiculoPlaca(): string {
    const idUsuario = this.getIdUsuarioAuth();
    return idUsuario ? `easyazul:veiculoAtualPlaca:${idUsuario}` : 'placaVeiculoAtual';
  }

  private keyVeiculoRecenteId(): string {
    const idUsuario = this.getIdUsuarioAuth();
    return idUsuario ? `easyazul:veiculoRecenteId:${idUsuario}` : 'idVeiculoRecente';
  }

  private keyVeiculoRecentePlaca(): string {
    const idUsuario = this.getIdUsuarioAuth();
    return idUsuario ? `easyazul:veiculoRecentePlaca:${idUsuario}` : 'placaVeiculoRecente';
  }

  private getVeiculoAtualIdFromStorage(): number {
    return Number(localStorage.getItem(this.keyVeiculoId()) ?? 0);
  }

  private getVeiculoAtualPlacaFromStorage(): string | null {
    return localStorage.getItem(this.keyVeiculoPlaca());
  }

  ngAfterViewInit(): void {
    this.syncVeiculoAtualFromStorage();
    this.syncVeiculoRecenteFromStorage();
    this.carregarVeiculoRecente();

    this.map = L.map('map', {
      zoomControl: true,
      scrollWheelZoom: true,
    }).setView([-26.9169, -49.0707], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);

    this.cluster = L.markerClusterGroup({
      showCoverageOnHover: false,
      spiderfyOnMaxZoom: true,
      maxClusterRadius: 40,
      iconCreateFunction: (c) => {
        const markers = c.getAllChildMarkers() as any[];
        const cores: string[] = markers.map((m) => m.options?.__pinCor as string);

        const cor = cores.includes('vermelho')
          ? 'vermelho'
          : cores.includes('amarelo')
            ? 'amarelo'
            : 'verde';

        return L.divIcon({
          className: `cluster cluster-${cor}`,
          html: `<span>${c.getChildCount()}</span>`,
          iconSize: L.point(96, 96),
        });
      },
    });

    this.map.addLayer(this.cluster);

    this.map.whenReady(() => {
      setTimeout(() => {
        this.invalidateMapLater();
        this.carregarZonas();

        this.capturarMinhaLocalizacaoSilenciosa().then(() => {
          if (this.minhaPosicao) {
            this.criarOuAtualizarMarkerUsuario(
              this.minhaPosicao.lat,
              this.minhaPosicao.lng,
              this.minhaPosicao.accuracy
            );
            this.atualizarZonasProximas();
          }
        });
      }, 0);
    });
  }

  // ===== modal msg =====
  protected abrirModalMsg(titulo: string, texto: string, tipo: 'ok' | 'bad' = 'ok'): void {
    this.modalMsgTitulo = titulo;
    this.modalMsgTexto = texto;
    this.modalMsgTipo = tipo;
    this.modalMsgAberto = true;
    this.cdr.detectChanges();
  }

  protected fecharModalMsg(): void {
    this.modalMsgAberto = false;
    this.cdr.detectChanges();
  }

  private extractMsg(err: any, fallback: string): string {
    return err?.error?.message || err?.error?.mensagem || err?.error?.erro || err?.message || fallback;
  }

  private invalidateMapLater(): void {
    requestAnimationFrame(() => this.map.invalidateSize(true));
    setTimeout(() => this.map.invalidateSize(true), 150);
    setTimeout(() => this.map.invalidateSize(true), 450);
  }

  public voltarHome(): void {
    this.router.navigateByUrl('/home');
  }

  public alternarTodasZonas(): void {
    this.mostrarTodasZonas = !this.mostrarTodasZonas;
    this.cdr.detectChanges();
  }

  // ===== reservar =====
  public reservarTicket(): void {
    if (!this.zonaSelecionada) return;

    const idVeiculo = this.veiculoAtualId ?? this.getVeiculoAtualIdFromStorage();

    if (!idVeiculo) {
      this.reservaPendenteZonaId = this.zonaSelecionada.idZona;
      this.abrirModalVeiculo();
      return;
    }

    this.efetivarReserva(this.zonaSelecionada.idZona, idVeiculo);
  }

  private efetivarReserva(idZona: number, idVeiculo: number): void {
    const payload = {
      idZona: idZona,
      idZonaEstacionamento: idZona,
      idVeiculo: idVeiculo,
    };

    this.http.post(`${environment.apiUrl}/tickets/reservar`, payload).subscribe({
      next: () => {
        const placa =
          this.veiculoAtualId === idVeiculo
            ? this.veiculoAtualPlaca
            : this.veiculos.find((v) => v.id === idVeiculo)?.placa ?? null;

        this.registrarVeiculoRecente(idVeiculo, placa);
        this.abrirModalMsg('Reserva realizada!', 'Sua reserva foi criada com sucesso.', 'ok');
        this.carregarZonas();
      },
      error: (err) => {
        console.error('Erro ao reservar:', err);
        const msg = this.extractMsg(err, 'Não foi possível reservar. Verifique login/permissões e regras da zona.');
        this.abrirModalMsg('Não foi possível reservar', msg, 'bad');
      },
    });
  }

  // ===== fullscreen =====
  public alternarFullscreen(): void {
    this.fullscreen = !this.fullscreen;

    setTimeout(() => {
      this.invalidateMapLater();
      if (this.zonas.length > 0) this.ajustarBounds();
    }, 220);
  }

  private ajustarBounds(): void {
    if (!this.zonas?.length) return;

    if (this.zonas.length === 1) {
      const z = this.zonas[0];
      this.map.setView([z.latitude, z.longitude], Math.max(this.map.getZoom(), 15));
      return;
    }

    const bounds = this.zonas.map((z) => [z.latitude, z.longitude] as [number, number]);
    this.map.fitBounds(bounds as any, { padding: [40, 40] });
  }

  // ===== zonas =====
  private carregarZonas(): void {
    this.carregandoZonas = true;
    this.erroZonas = null;
    this.cdr.detectChanges();

    this.http.get<any>(`${environment.apiUrl}/mapa/zonas`).subscribe({
      next: (res) => {
        const lista = Array.isArray(res) ? res : res?.content ?? [];
        this.cluster.clearLayers();

        const normalizadas = (lista as ZonaMapa[])
          .map((z) => this.normalizarZona(z))
          .filter((z): z is ZonaNormalizada => z !== null);

        this.zonas = normalizadas;

        if (this.zonas.length === 0) {
          this.carregandoZonas = false;
          this.invalidateMapLater();
          this.cdr.detectChanges();
          return;
        }

        this.zonas.forEach((z, idx) => {
          const cor = this.corDoPin(z);
          const icon = this.iconePinGoogleMapsLike(z, idx);

          const marker = L.marker([z.latitude, z.longitude], {
            icon: icon as any,
            __pinCor: cor,
          } as any)
            .bindPopup(this.popupHtml(z))
            .on('click', () => {
              // ESSENCIAL: Leaflet roda fora do Angular -> sem isso precisa "2 cliques"
              this.ngZone.run(() => {
                this.zonaSelecionada = z;
                this.mostrarTodasZonas = false;
                this.cdr.detectChanges();
              });

              this.map.setView([z.latitude, z.longitude], Math.max(this.map.getZoom(), 15));
            });

          this.cluster.addLayer(marker);
        });

        this.invalidateMapLater();
        this.ajustarBounds();
        this.atualizarZonasProximas();

        this.carregandoZonas = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar /mapa/zonas:', err);
        this.erroZonas = 'Não foi possível carregar as zonas.';
        this.carregandoZonas = false;
        this.invalidateMapLater();
        this.cdr.detectChanges();
      },
    });
  }

  private toNumber(v: any): number {
    if (v === null || v === undefined) return NaN;
    if (typeof v === 'number') return v;
    const s = String(v).trim().replace(',', '.');
    return Number(s);
  }

  private normalizarZona(z: ZonaMapa): ZonaNormalizada | null {
    const idZona = Number((z as any).idZona ?? (z as any).idZonaEstacionamento ?? (z as any).id ?? 0);
    if (!idZona) return null;

    const lat = this.toNumber(z.latitude);
    const lng = this.toNumber(z.longitude);

    const latOk = Number.isFinite(lat) && Math.abs(lat) <= 90;
    const lngOk = Number.isFinite(lng) && Math.abs(lng) <= 180;
    if (!latOk || !lngOk) return null;

    const capacidade = Math.max(0, this.toNumber(z.capacidadeVagas ?? 0) || 0);
    const disponiveis = Math.max(0, this.toNumber(z.vagasDisponiveis ?? 0) || 0);
    const ocupadas = Math.max(0, this.toNumber(z.vagasOcupadas ?? 0) || 0);

    return {
      idZona,
      nome: String(z.nome ?? 'Zona'),
      latitude: lat,
      longitude: lng,
      capacidadeVagas: capacidade,
      vagasDisponiveis: disponiveis,
      vagasOcupadas: ocupadas,
    };
  }

  public corDoPin(z: ZonaNormalizada): 'verde' | 'amarelo' | 'vermelho' {
    const capacidade = z.capacidadeVagas ?? 0;
    const disponiveis = z.vagasDisponiveis ?? 0;

    if (capacidade <= 0) return 'verde';
    if (disponiveis <= 0) return 'vermelho';

    const proporcaoLivre = disponiveis / capacidade;
    if (proporcaoLivre <= 0.5) return 'amarelo';

    return 'verde';
  }

  public statusClass(z: ZonaNormalizada): 'ok' | 'warn' | 'bad' {
    const cor = this.corDoPin(z);
    return cor === 'verde' ? 'ok' : cor === 'amarelo' ? 'warn' : 'bad';
  }

  public statusLabel(z: ZonaNormalizada): string {
    const cor = this.corDoPin(z);
    return cor === 'verde' ? 'Disponível' : cor === 'amarelo' ? 'Quase lotado' : 'Lotado';
  }

  // ===== pin =====
  private iconePinGoogleMapsLike(z: ZonaNormalizada, index: number): L.DivIcon {
    const cor = this.corDoPin(z);
    const hex = cor === 'verde' ? '#2ecc71' : cor === 'amarelo' ? '#f1c40f' : '#e74c3c';
    const delayMs = Math.min(index * 35, 400);

    return L.divIcon({
      className: '',
      html: `
        <span
          class="material-icons pin-gm pin-${cor}"
          style="--d:${delayMs}ms; color:${hex}; font-size:80px; line-height:80px;"
        >place</span>
      `,
      iconSize: [75, 75],
      iconAnchor: [40, 75],
      popupAnchor: [0, -75],
    });
  }

  private popupHtml(z: ZonaNormalizada): string {
    const destino = `${z.latitude},${z.longitude}`;
    const url = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(destino)}`;

    const label =
      z.vagasDisponiveis <= 0 ? 'Lotado' : this.corDoPin(z) === 'amarelo' ? 'Quase lotado' : 'Disponível';

    return `
      <div class="pz">
        <div class="pz-title">${z.nome}</div>
        <div class="pz-sub">${label} • ${z.vagasDisponiveis}/${z.capacidadeVagas} vagas livres</div>
        <a class="gmaps-btn" href="${url}" target="_blank" rel="noopener">
          <span class="material-icons">near_me</span>
          Abrir no Maps
        </a>
      </div>
    `;
  }

  public focarZona(z: ZonaNormalizada): void {
    this.zonaSelecionada = z;
    this.mostrarTodasZonas = false;
    this.map.setView([z.latitude, z.longitude], 16);
    this.cdr.detectChanges();
  }

  public limparSelecao(): void {
    this.zonaSelecionada = null;
    this.cdr.detectChanges();
  }

  // ===== geolocalizacao =====
  public minhaLocalizacao(): void {
    if (!navigator.geolocation) return;

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        const accuracy = pos.coords.accuracy;

        this.minhaPosicao = { lat, lng, accuracy };
        this.criarOuAtualizarMarkerUsuario(lat, lng, accuracy);

        this.map.setView([lat, lng], 16);
        this.atualizarZonasProximas();
      },
      (err) => console.error('Erro geolocalização:', err),
      { enableHighAccuracy: true, timeout: 8000 }
    );
  }

  private capturarMinhaLocalizacaoSilenciosa(): Promise<void> {
    return new Promise((resolve) => {
      if (!navigator.geolocation) return resolve();

      navigator.geolocation.getCurrentPosition(
        (pos) => {
          this.minhaPosicao = { lat: pos.coords.latitude, lng: pos.coords.longitude, accuracy: pos.coords.accuracy };
          resolve();
        },
        () => resolve(),
        { enableHighAccuracy: true, timeout: 6000 }
      );
    });
  }

  private criarOuAtualizarMarkerUsuario(lat: number, lng: number, accuracy?: number): void {
    const icon = this.iconeUsuarioAzul();

    if (!this.userMarker) {
      this.userMarker = L.marker([lat, lng], { icon: icon as any, zIndexOffset: 9999 }).addTo(this.map);
    } else {
      this.userMarker.setLatLng([lat, lng]);
      (this.userMarker as any).setIcon(icon);
    }

    if (accuracy && accuracy > 0) {
      const radius = Math.min(Math.max(accuracy, 15), 160);

      if (!this.userAccuracyCircle) {
        this.userAccuracyCircle = L.circle([lat, lng], {
          radius,
          weight: 1,
          opacity: 0.65,
          fillOpacity: 0.12,
        }).addTo(this.map);
      } else {
        this.userAccuracyCircle.setLatLng([lat, lng]);
        this.userAccuracyCircle.setRadius(radius);
      }
    }
  }

  private iconeUsuarioAzul(): L.DivIcon {
    return L.divIcon({
      className: '',
      html: `
        <span class="material-icons user-pin-blue"
          style="color:#1e88e5; font-size:72px; line-height:72px;"
        >person_pin_circle</span>
      `,
      iconSize: [72, 72],
      iconAnchor: [36, 72],
      popupAnchor: [0, -72],
    });
  }

  public async abrirRotaNoGoogleMaps(z: ZonaNormalizada): Promise<void> {
    const destino = `${z.latitude},${z.longitude}`;

    if (!this.minhaPosicao) await this.capturarMinhaLocalizacaoSilenciosa();

    if (!this.minhaPosicao) {
      const fallback = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(destino)}`;
      window.open(fallback, '_blank');
      return;
    }

    const origem = `${this.minhaPosicao.lat},${this.minhaPosicao.lng}`;
    const url =
      `https://www.google.com/maps/dir/?api=1` +
      `&origin=${encodeURIComponent(origem)}` +
      `&destination=${encodeURIComponent(destino)}` +
      `&travelmode=driving`;

    window.open(url, '_blank');
  }

  private atualizarZonasProximas(): void {
    if (!this.minhaPosicao || !this.zonas?.length) {
      this.zonasProximas = [];
      this.cdr.detectChanges();
      return;
    }

    const { lat, lng } = this.minhaPosicao;

    this.zonasProximas = this.zonas
      .map((z) => ({ ...z, distanciaKm: this.distanciaKm(lat, lng, z.latitude, z.longitude) }))
      .sort((a, b) => a.distanciaKm - b.distanciaKm)
      .slice(0, 5);

    this.cdr.detectChanges();
  }

  private distanciaKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371;
    const dLat = this.deg2rad(lat2 - lat1);
    const dLon = this.deg2rad(lon2 - lon1);

    const a =
      Math.sin(dLat / 2) ** 2 +
      Math.cos(this.deg2rad(lat1)) * Math.cos(this.deg2rad(lat2)) * Math.sin(dLon / 2) ** 2;

    return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private deg2rad(deg: number): number {
    return deg * (Math.PI / 180);
  }

  // ===== veiculo =====
  private carregarVeiculoRecente(): void {
    const url = `${environment.apiUrl}/tickets/meus?size=1&sort=inicioTicket,desc`;

    this.http.get<any>(url).subscribe({
      next: (res) => {
        // Se o usuário já trocou o veículo enquanto a requisição carregava,
        // a escolha manual deve prevalecer.
        if (this.veiculoAlteradoManualmente) return;

        const tickets: TicketRecente[] = Array.isArray(res)
          ? res
          : res?.content ?? [];

        const ultimoTicket = tickets[0];
        const idVeiculo = Number(ultimoTicket?.idVeiculo ?? 0);

        if (!idVeiculo) return;

        const placa = ultimoTicket?.placa
          ? String(ultimoTicket.placa).trim().toUpperCase()
          : null;

        this.registrarVeiculoRecente(idVeiculo, placa);
        this.definirVeiculoAtual(idVeiculo, placa, true);
        this.cdr.detectChanges();
      },
      error: (err) => {
        // Não bloqueia a tela: mantém o veículo salvo no localStorage como fallback.
        console.warn('Não foi possível carregar o veículo usado recentemente:', err);
      },
    });
  }

  private registrarVeiculoRecente(
    idVeiculo: number,
    placa: string | null
  ): void {
    const placaNormalizada = placa?.trim()
      ? placa.trim().toUpperCase()
      : null;

    this.veiculoRecenteId = idVeiculo;
    this.veiculoAtualEhRecente = this.veiculoAtualId === idVeiculo;

    localStorage.setItem(this.keyVeiculoRecenteId(), String(idVeiculo));

    if (placaNormalizada) {
      localStorage.setItem(this.keyVeiculoRecentePlaca(), placaNormalizada);
    } else {
      localStorage.removeItem(this.keyVeiculoRecentePlaca());
    }

    this.cdr.detectChanges();
  }

  private syncVeiculoRecenteFromStorage(): void {
    const id = Number(localStorage.getItem(this.keyVeiculoRecenteId()) ?? 0);

    this.veiculoRecenteId = id || null;
    this.veiculoAtualEhRecente = !!id && this.veiculoAtualId === id;

    if (this.veiculoAtualEhRecente && !this.veiculoAtualPlaca) {
      const placa = localStorage.getItem(this.keyVeiculoRecentePlaca());
      this.veiculoAtualPlaca = placa ? String(placa).toUpperCase() : null;
    }

    this.cdr.detectChanges();
  }

  private definirVeiculoAtual(
    idVeiculo: number,
    placa: string | null,
    ehRecente: boolean
  ): void {
    const placaNormalizada = placa?.trim()
      ? placa.trim().toUpperCase()
      : null;

    this.veiculoAtualId = idVeiculo;
    this.veiculoAtualPlaca = placaNormalizada;
    this.veiculoAtualEhRecente = ehRecente;

    localStorage.setItem(this.keyVeiculoId(), String(idVeiculo));

    if (placaNormalizada) {
      localStorage.setItem(this.keyVeiculoPlaca(), placaNormalizada);
    } else {
      localStorage.removeItem(this.keyVeiculoPlaca());
    }
  }

  public get veiculoAtualOrigemLabel(): string {
    if (!this.veiculoAtualId) return 'Veículo';
    return this.veiculoAtualEhRecente ? 'Usado recentemente' : 'Veículo selecionado';
  }

  public get veiculoAtualLabel(): string {
    if (this.veiculoAtualPlaca?.trim()) return this.veiculoAtualPlaca;
    if (this.veiculoAtualId) return `#${this.veiculoAtualId}`;
    return 'Nenhum veículo selecionado';
  }

  public abrirTrocarVeiculo(): void {
    this.abrirModalVeiculo();
  }

  protected abrirModalVeiculo(): void {
    this.modalVeiculoAberto = true;
    this.erroVeiculos = null;

    const atual = this.veiculoAtualId ?? this.getVeiculoAtualIdFromStorage();
    this.veiculoSelecionadoId = atual || null;

    // se ja carregou uma vez, nao refaz request
    if (this.veiculos.length > 0) {
      this.cdr.detectChanges();
      return;
    }

    this.carregandoVeiculos = true;
    this.cdr.detectChanges();

    const url = `${environment.apiUrl}/veiculos/meus`;

    this.http
      .get<any>(url)
      .pipe(
        finalize(() => {
          this.carregandoVeiculos = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (res) => {
          const lista = Array.isArray(res) ? res : res?.content ?? [];

          this.veiculos = (lista as Veiculo[])
            .map((v) => this.normalizarVeiculo(v))
            .filter((v): v is VeiculoTela => v !== null);

          if (atual && this.veiculos.some((x) => x.id === atual)) {
            const v = this.veiculos.find((x) => x.id === atual)!;
            if (!this.veiculoAtualPlaca) this.veiculoAtualPlaca = v.placa;
          }

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Erro ao carregar veículos:', err);
          this.erroVeiculos = 'Não foi possível carregar seus veículos.';
          this.cdr.detectChanges();
        },
      });
  }

  protected fecharModalVeiculo(): void {
    this.modalVeiculoAberto = false;
    this.erroVeiculos = null;
    this.cdr.detectChanges();
  }

  protected selecionarVeiculo(v: VeiculoTela): void {
    this.veiculoSelecionadoId = v.id;
    this.cdr.detectChanges();
  }

  protected confirmarVeiculo(): void {
    if (!this.veiculoSelecionadoId) {
      this.erroVeiculos = 'Selecione um veículo para continuar.';
      this.cdr.detectChanges();
      return;
    }

    const v = this.veiculos.find((x) => x.id === this.veiculoSelecionadoId);
    const placa = v?.placa ?? null;

    this.veiculoAlteradoManualmente = true;
    this.definirVeiculoAtual(this.veiculoSelecionadoId, placa, false);

    this.fecharModalVeiculo();

    if (this.reservaPendenteZonaId && this.veiculoAtualId) {
      const zonaId = this.reservaPendenteZonaId;
      this.reservaPendenteZonaId = null;
      this.efetivarReserva(zonaId, this.veiculoAtualId);
    }
  }

  protected irParaCadastroVeiculo(): void {
    this.fecharModalVeiculo();
    this.router.navigateByUrl('/veiculos');
  }

  private normalizarVeiculo(v: Veiculo): VeiculoTela | null {
    const id = Number(v.idVeiculo ?? v.id ?? 0);
    if (!id) return null;

    return {
      id,
      placa: String(v.placa ?? '').toUpperCase(),
      tipoVeiculo: String(v.tipoVeiculo ?? 'VEÍCULO'),
      status: v.status ? String(v.status) : undefined,
    };
  }

  private syncVeiculoAtualFromStorage(): void {
    const id = this.getVeiculoAtualIdFromStorage();
    const placa = this.getVeiculoAtualPlacaFromStorage();

    this.veiculoAtualId = id || null;
    this.veiculoAtualPlaca = placa ? String(placa) : null;
    this.veiculoAtualEhRecente = false;

    this.cdr.detectChanges();
  }

  // ===== auth =====
  private getAuth(): AuthStore | null {
    const raw = localStorage.getItem('easyazul_auth');
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthStore;
    } catch {
      return null;
    }
  }

  private getIdUsuarioAuth(): number {
    const a = this.getAuth();
    return Number(a?.idUsuario ?? 0);
  }
}

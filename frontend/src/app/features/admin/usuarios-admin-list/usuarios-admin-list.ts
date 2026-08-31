import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ChangeDetectorRef, NgZone } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { UsuarioService, UsuarioDetalhe, Page } from '../../../core/api/usuario.service/usuario.service';

@Component({
  selector: 'app-usuarios-admin-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios-admin-list.html',
  styleUrl: './usuarios-admin-list.scss',
})
export class UsuariosAdminListComponent implements OnInit {
  carregando = false;
  erro: string | null = null;

  busca = '';
  page = 0;
  size = 10;

  pageData: Page<UsuarioDetalhe> | null = null;
  usuariosFiltrados: UsuarioDetalhe[] = [];

  constructor(
    private api: UsuarioService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.erro = null;

    this.api
      .listarAdmin(this.page, this.size, 'nome')
      .pipe(finalize(() => {
        this.carregando = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (p) => {
          this.zone.run(() => {
            this.pageData = p;

            const list = p.content ?? [];
            const f = (this.busca ?? '').trim().toLowerCase();

            this.usuariosFiltrados = !f
              ? list
              : list.filter((u) => {
                const nome = (u.nome ?? '').toLowerCase();
                const email = (u.email ?? '').toLowerCase();
                const tipo = (u.tipo ?? '').toLowerCase();
                const telefone = (u.telefone ?? '').toLowerCase();
                return (
                  nome.includes(f) ||
                  email.includes(f) ||
                  tipo.includes(f) ||
                  telefone.includes(f)
                );
              });

            this.cdr.detectChanges();
          });
        },
        error: (err) => {
          console.error(err);
          this.zone.run(() => {
            this.erro = 'Não foi possível carregar os usuários.';
            this.pageData = null;
            this.usuariosFiltrados = [];
            this.cdr.detectChanges();
          });
        },
      });
  }

  aplicarBusca(): void {
    const list: UsuarioDetalhe[] = this.pageData?.content ?? [];
    const f = (this.busca ?? '').trim().toLowerCase();

    let filtrados: UsuarioDetalhe[];

    if (!f) {
      filtrados = list;
    } else {
      filtrados = list.filter((u: UsuarioDetalhe) => {
        const nome = (u.nome ?? '').toLowerCase();
        const email = (u.email ?? '').toLowerCase();
        const tipo = (u.tipo ?? '').toLowerCase();
        const telefone = (u.telefone ?? '').toLowerCase();
        return nome.includes(f) || email.includes(f) || tipo.includes(f) || telefone.includes(f);
      });
    }

    this.usuariosFiltrados = this.ordenarPorStatus(filtrados);
  }


  novo(): void {
    this.router.navigateByUrl('/admin/usuarios/novo');
  }

  detalhar(u: UsuarioDetalhe): void {
    const html = `
    <div class="grid">
      <div><span>Número do ID: </span><b>${u.id}</b></div>
      <div><span>Nome: </span><b>${u.nome}</b></div>
      <div><span>E-mail: </span><b>${u.email}</b></div>
      <div><span>Telefone: </span><b>${u.telefone}</b></div>
      <div><span>Tipo: </span><b>${u.tipo}</b></div>
      <div><span>Status: </span><b>${u.status}</b></div>
      <div><span>Razão social: </span><b>${u.razaoSocial ?? '-'}</b></div>
      <div><span>Data de nascimento: </span><b>${this.formatarDataBR(u.dataNascimento)}</b></div>
    </div>
  `;
    this.abrirModal('Detalhes do Usuário', html);
  }

  editar(u: UsuarioDetalhe): void {
    if (u.status !== 'ATIVO') {
      const html = `
      <p>Não é possível editar um usuário <b>INATIVO</b>.</p>
      <p>Use o botão <b>Reativar</b> para ativá-lo novamente.</p>
    `;
      this.abrirModal('Ação não permitida', html);
      return;
    }
    this.router.navigateByUrl(`/admin/usuarios/${u.id}/editar`);
  }

  reativar(u: UsuarioDetalhe): void {
    const html = `<p>Deseja <b>reativar</b> o usuário <b>${u.nome}</b>?</p>`;
    this.abrirModal('Confirmar reativação', html, () => {
      this.api.reativar(u.id).subscribe({
        next: () => { this.fecharModal(); this.carregar(); },
        error: (err) => { this.erro = 'Não foi possível reativar o usuário.'; console.error(err); }
      });
    });
  }

  inativar(u: UsuarioDetalhe): void {
    const html = `<p>Deseja <b>inativar</b> o usuário <b>${u.nome}</b>?</p>`;
    this.abrirModal('Confirmar inativação', html, () => {
      this.api.inativar(u.id).subscribe({
        next: () => { this.fecharModal(); this.carregar(); },
        error: (err) => { this.erro = 'Não foi possível inativar o usuário.'; console.error(err); }
      });
    });
  }

  get isFirst(): boolean {
    return (this.page ?? 0) <= 0;
  }

  get isLast(): boolean {
    if (!this.pageData) return true;
    return (this.page ?? 0) >= (this.pageData.totalPages - 1);
  }

  prev(): void {
    if (this.isFirst) return;
    this.page -= 1;
    this.carregar();
  }

  next(): void {
    if (this.isLast) return;
    this.page += 1;
    this.carregar();
  }

  modalAberto = false;
  modalTitulo = '';
  modalHtml = '';
  acaoConfirmar: (() => void) | null = null;

  abrirModal(titulo: string, html: string, confirmar?: () => void) {
    this.modalTitulo = titulo;
    this.modalHtml = html;
    this.acaoConfirmar = confirmar ?? null;
    this.modalAberto = true;
  }

  fecharModal() {
    this.modalAberto = false;
    this.acaoConfirmar = null;
  }

  formatarDataBR(data: string | null | undefined): string {
    if (!data) return '-';

    const [ano, mes, dia] = data.split('-');
    if (!ano || !mes || !dia) return data;

    return `${dia}/${mes}/${ano}`;
  }

  toastMsg: string | null = null;
  private toastTimer: any = null;

  private toast(msg: string) {
    this.toastMsg = msg;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => (this.toastMsg = null), 2500);
  }

  private statusRank(status: string | null | undefined): number {
    return (status ?? '').toUpperCase() === 'ATIVO' ? 0 : 1;
  }

  private ordenarPorStatus(lista: UsuarioDetalhe[]): UsuarioDetalhe[] {
    return [...lista].sort((a, b) => this.statusRank(a.status) - this.statusRank(b.status));
  }

}

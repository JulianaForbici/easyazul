import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { UsuarioService, UsuarioDetalhe, DadosAtualizacaoUsuario} from '../../../core/api/usuario.service/usuario.service';
import {MaskInputDirective} from '../../../core/shared/masks/mask-input.directive';

@Component({
  selector: 'app-usuarios-admin-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, MaskInputDirective],
  templateUrl: './usuarios-admin-edit.html',
  styleUrl: './usuarios-admin-edit.scss',
})
export class UsuariosAdminEditComponent implements OnInit {
  carregando = false;
  salvando = false;
  erro: string | null = null;
  okMsg: string | null = null;

  id!: number;
  usuario: UsuarioDetalhe | null = null;

  // form
  nome = '';
  email = '';
  telefone = '';
  cpf: string | null = null;
  cnpj: string | null = null;

  senha: string | null = null;
  confirmarSenha: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: UsuarioService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(pm => {
      const idStr = pm.get('id');
      const id = Number(idStr);
      console.log('param idStr=', idStr, '-> id=', id);

      this.id = id;
      void this.carregar();
    });
  }

  get podeEditar(): boolean {
    return this.usuario?.status === 'ATIVO';
  }

  async carregar(): Promise<void> {
    this.zone.run(() => {
      this.carregando = true;
      this.erro = null;
      this.cdr.detectChanges();
    });

    console.log('Editando usuário id=', this.id);

    if (!this.id || this.id <= 0 || Number.isNaN(this.id)) {
      this.zone.run(() => {
        this.erro = 'ID inválido na rota.';
        this.carregando = false;
        this.cdr.detectChanges();
      });
      return;
    }

    try {
      const u = await firstValueFrom(this.api.detalhar(this.id));
      console.log('RETORNO DETALHAR:', u);

      this.zone.run(() => {
        this.usuario = u;

        if (u.status !== 'ATIVO') {
          this.erro = 'Usuário INATIVO não pode ser editado. Reative o usuário para continuar.';
        } else {
          this.nome = u.nome ?? '';
          this.email = u.email ?? '';
          this.telefone = u.telefone ?? '';
          this.cpf = (u as any).cpf ?? null;
          this.cnpj = (u as any).cnpj ?? null;
        }

        this.carregando = false;
        this.cdr.detectChanges();
      });
    } catch (err: any) {
      console.error('Erro ao carregar usuário:', err);

      this.zone.run(() => {
        this.erro = err?.error?.message ?? 'Não foi possível carregar o usuário.';
        this.carregando = false;
        this.cdr.detectChanges();
      });
    }
  }

  salvar(): void {
    if (!this.podeEditar) {
      this.erro = 'Usuário INATIVO não pode ser editado.';
      return;
    }

    this.okMsg = null;
    this.erro = null;

    const senhaLimpa = (this.senha ?? '').trim();
    const confirmarLimpa = (this.confirmarSenha ?? '').trim();

    if (senhaLimpa || confirmarLimpa) {
      if (senhaLimpa.length < 6) {
        this.erro = 'A senha deve ter pelo menos 6 caracteres.';
        return;
      }
      if (senhaLimpa !== confirmarLimpa) {
        this.erro = 'As senhas não conferem.';
        return;
      }
      this.senha = senhaLimpa;
    } else {
      this.senha = null;
    }

    const dados: DadosAtualizacaoUsuario = {
      nome: this.nome.trim(),
      email: this.email.trim(),
      telefone: this.onlyDigits(this.telefone),
      cpf: this.onlyDigits(this.cpf) || null,
      cnpj: this.onlyDigits(this.cnpj) || null,
      senha: this.senha,
    };

    this.salvando = true;

    this.api.atualizar(this.id, dados).subscribe({
      next: () => {
        this.salvando = false;
        this.okMsg = 'Usuário atualizado com sucesso!';
        setTimeout(() => this.router.navigateByUrl('/admin/usuarios'), 600);
      },
      error: (err: any) => {
        console.error(err);
        this.salvando = false;
        this.erro = err?.error?.message ?? 'Não foi possível atualizar o usuário.';
      },
    });
  }

  voltar(): void {
    this.router.navigateByUrl('/admin/usuarios');
  }

  formatarDataBR(data: string | null | undefined): string {
    if (!data) return '-';
    const [ano, mes, dia] = data.split('-');
    return ano && mes && dia ? `${dia}/${mes}/${ano}` : data;
  }

  private onlyDigits(v: string | null | undefined): string {
    return (v ?? '').replace(/\D/g, '');
  }
}

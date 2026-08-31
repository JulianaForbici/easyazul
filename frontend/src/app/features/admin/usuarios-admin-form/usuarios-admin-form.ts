import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';

import {
  UsuarioService,
  DadosCadastroUsuario,
  TipoUsuario
} from '../../../core/api/usuario.service/usuario.service';
import { MaskInputDirective } from '../../../core/shared/masks/mask-input.directive';
import {Observable} from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-usuarios-admin-form',
  imports: [CommonModule, FormsModule, MaskInputDirective],
  templateUrl: './usuarios-admin-form.html',
  styleUrl: './usuarios-admin-form.scss',
})
export class UsuariosAdminFormComponent {
  carregando = false;
  erroGeral: string | null = null;

  toastMsg: string | null = null;
  private toastTimer: any = null;

  readonly hoje = this.formatDateInput(new Date());
  readonly minNascimento = '1900-01-01';

  form: DadosCadastroUsuario = {
    nome: '',
    email: '',
    senha: '',
    telefone: '',
    tipo: 'FISCAL',
    cpf: null,
    cnpj: null,
    dataNascimento: null,
    razaoSocial: null,
  };

  constructor(private api: UsuarioService, private router: Router) {}

  tiposAdmin: { label: string; value: TipoUsuario }[] = [
    { label: 'Administrador', value: 'ADMINISTRADOR' },
    { label: 'Fiscal', value: 'FISCAL' },
    { label: 'Motorista', value: 'MOTORISTA' },
    { label: 'Empresa', value: 'EMPRESA' },
  ];

  onTipoChange(): void {
    if (this.form.tipo === 'EMPRESA') {
      this.form.cpf = null;
    } else {
      this.form.cnpj = null;
      this.form.razaoSocial = null;
    }
  }

  private onlyDigits(v: string | null | undefined): string | null {
    const out = (v ?? '').replace(/\D/g, '');
    return out ? out : null;
  }

  private toast(msg: string) {
    this.toastMsg = msg;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => (this.toastMsg = null), 2500);
  }

  cadastrar(f: NgForm): void {
    this.erroGeral = null;

    if (f.invalid) {
      this.erroGeral = 'Verifique os campos do formulário.';
      return;
    }

    const telDigits = (this.form.telefone ?? '').replace(/\D/g, '');
    if (!(telDigits.length === 10 || telDigits.length === 11)) {
      this.erroGeral = 'Telefone inválido.';
      return;
    }

    if (this.form.tipo === 'EMPRESA') {
      const cnpjDigits = this.onlyDigits(this.form.cnpj);
      if (!cnpjDigits || cnpjDigits.length !== 14) {
        this.erroGeral = 'CNPJ inválido.';
        return;
      }
      if (!String(this.form.razaoSocial ?? '').trim()) {
        this.erroGeral = 'Razão social é obrigatória para empresa.';
        return;
      }
    } else {
      const cpfDigits = this.onlyDigits(this.form.cpf);
      if (cpfDigits && cpfDigits.length !== 11) {
        this.erroGeral = 'CPF inválido.';
        return;
      }
    }

    const payload: DadosCadastroUsuario = {
      nome: this.form.nome.trim(),
      email: this.form.email.trim(),
      senha: this.form.senha,
      telefone: telDigits,
      tipo: this.form.tipo,
      dataNascimento: this.form.dataNascimento || null,
      cpf: this.form.tipo !== 'EMPRESA' ? this.onlyDigits(this.form.cpf) : null,
      cnpj: this.form.tipo === 'EMPRESA' ? this.onlyDigits(this.form.cnpj) : null,
      razaoSocial:
        this.form.tipo === 'EMPRESA'
          ? (String(this.form.razaoSocial ?? '').trim() || null)
          : null,
    };

    this.carregando = true;

    this.api
      .cadastrarAdmin(payload)
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: () => {
          this.toast('Usuário criado com sucesso!');

          f.resetForm({
            nome: '',
            email: '',
            senha: '',
            telefone: '',
            tipo: 'FISCAL',
            cpf: null,
            cnpj: null,
            dataNascimento: null,
            razaoSocial: null,
          } as any);

          setTimeout(() => this.router.navigateByUrl('/admin/usuarios'), 900);
        },
        error: (err: unknown) => {
          console.error(err);
          this.erroGeral = 'Não foi possível criar o usuário. Verifique os dados.';
        },
      });
  }

  voltar(): void {
    this.router.navigateByUrl('/admin/usuarios');
  }

  private formatDateInput(d: Date): string {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

}

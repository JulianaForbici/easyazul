import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  UsuarioService,
  DadosAtualizacaoUsuario,
  UsuarioDetalhe,
  TipoUsuario,
} from '../../../core/api/usuario.service/usuario.service';
import { AuthService } from '../../../core/auth/auth.service';
import { MaskInputDirective } from '../../../core/shared/masks/mask-input.directive';
import { finalize } from 'rxjs/operators';

type ToastVariant = 'ok' | 'warn' | 'err';

@Component({
  standalone: true,
  selector: 'app-perfil',
  imports: [CommonModule, FormsModule, MaskInputDirective],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss',
})
export class PerfilComponent implements OnInit, OnDestroy {
  carregando = true;
  salvando = false;

  usuario: UsuarioDetalhe | null = null;

  // imagem do perfil (adicione em src/assets/images/parking.png)
  readonly parkingImg = 'assets/images/parking.png';

  form: DadosAtualizacaoUsuario = {
    nome: '',
    email: '',
    telefone: '',
    senha: null,
    cpf: null,
    cnpj: null,
  };

  // toast padrao
  toastMsg: string | null = null;
  toastVariant: ToastVariant = 'ok';
  private toastTimer: any = null;

  // modal de inativar
  modalInativarAberto = false;
  inativando = false;

  constructor(
    private api: UsuarioService,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = this.authService.getIdUsuario();

    if (!id) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.api
      .detalhar(id)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (u) => {
          this.usuario = u;

          this.form = {
            nome: u.nome ?? '',
            email: u.email ?? '',
            telefone: u.telefone ?? '',
            senha: null,
            cpf: u.cpf ?? null,
            cnpj: u.cnpj ?? null,
          };

          this.cdr.detectChanges();
        },
        error: (err) => {
          this.toast(this.extractMsg(err, 'Não foi possível carregar o seu perfil.'), 'err');
          this.cdr.detectChanges();
        },
      });
  }

  ngOnDestroy(): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
  }

  // ======= Ações principais =======

  salvar(): void {
    if (!this.usuario) return;

    this.salvando = true;
    this.clearToast();

    const tipo = (this.usuario.tipo ?? '').toUpperCase() as TipoUsuario;

    // telefone: envia só dígitos
    const telDigits = this.onlyDigits(this.form.telefone);
    if (!telDigits || !(telDigits.length === 10 || telDigits.length === 11)) {
      this.salvando = false;
      this.toast('Telefone inválido.', 'warn');
      this.cdr.detectChanges();
      return;
    }

    let cpf: string | null = null;
    let cnpj: string | null = null;

    if (['MOTORISTA', 'ADMINISTRADOR', 'FISCAL'].includes(tipo)) {
      cpf = this.onlyDigits(this.form.cpf);
      cnpj = null;
      if (cpf && cpf.length !== 11) {
        this.salvando = false;
        this.toast('CPF inválido.', 'warn');
        this.cdr.detectChanges();
        return;
      }
    } else if (tipo === 'EMPRESA') {
      cnpj = this.onlyDigits(this.form.cnpj);
      cpf = null;
      if (cnpj && cnpj.length !== 14) {
        this.salvando = false;
        this.toast('CNPJ inválido.', 'warn');
        this.cdr.detectChanges();
        return;
      }
    }

    const payload: DadosAtualizacaoUsuario = {
      nome: this.trimObrigatorio(this.form.nome),
      email: this.trimObrigatorio(this.form.email),
      telefone: telDigits,
      senha: this.vazioParaNull(this.form.senha),
      cpf,
      cnpj,
    };

    this.api
      .atualizar(this.usuario.id, payload)
      .pipe(
        finalize(() => {
          this.salvando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (u) => {
          this.usuario = u;

          // mantém o form sincronizado
          this.form.nome = u.nome ?? this.form.nome;
          this.form.email = u.email ?? this.form.email;
          this.form.telefone = u.telefone ?? this.form.telefone;
          this.form.senha = null;
          this.form.cpf = u.cpf ?? null;
          this.form.cnpj = u.cnpj ?? null;

          this.toast('Usuário editado com sucesso!', 'ok');
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.toast(this.extractMsgMelhorado(err, 'Não foi possível salvar. Verifique os campos.'), 'err');
          this.cdr.detectChanges();
        },
      });
  }

  abrirModalInativar(): void {
    if (!this.usuario) return;
    this.modalInativarAberto = true;
    this.clearToast();
    this.cdr.detectChanges();
  }

  fecharModalInativar(): void {
    if (this.inativando) return;
    this.modalInativarAberto = false;
    this.cdr.detectChanges();
  }

  confirmarInativar(): void {
    if (!this.usuario) return;

    this.inativando = true;
    this.clearToast();
    this.cdr.detectChanges();

    this.api
      .inativar(this.usuario.id)
      .pipe(
        finalize(() => {
          this.inativando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          localStorage.removeItem('easyazul_auth');
          this.router.navigateByUrl('/login');
        },
        error: (err) => {
          this.modalInativarAberto = false;
          this.toast(this.extractMsgMelhorado(err, 'Não foi possível inativar a sua conta.'), 'err');
          this.cdr.detectChanges();
        },
      });
  }

  // ======= Toast =======

  toastIcon(): string {
    if (this.toastVariant === 'ok') return 'check_circle';
    if (this.toastVariant === 'warn') return 'info';
    return 'error';
  }

  toast(msg: string, variant: ToastVariant): void {
    this.toastMsg = String(msg ?? '');
    this.toastVariant = variant;

    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => {
      this.toastMsg = null;
      this.cdr.detectChanges();
    }, 2600);
  }

  clearToast(): void {
    this.toastMsg = null;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = null;
  }

  // ======= Helpers =======

  private trimObrigatorio(v: any): string {
    return String(v ?? '').trim();
  }

  private vazioParaNull(v: any): string | null {
    const s = String(v ?? '').trim();
    return s ? s : null;
  }

  private onlyDigits(v: any): string | null {
    const s = String(v ?? '').replace(/\D/g, '');
    return s ? s : null;
  }

  private extractMsg(err: any, fallback: string): string {
    return (
      err?.error?.message ||
      err?.error?.mensagem ||
      err?.error?.erro ||
      err?.message ||
      fallback
    );
  }

  private extractMsgMelhorado(err: any, fallback: string): string {
    const e = err?.error;

    if (Array.isArray(e)) {
      const msgs = e
        .map((x: any) => x?.message || x?.mensagem || x?.erro)
        .filter(Boolean);
      if (msgs.length) return msgs.join(' | ');
    }

    if (e?.errors && Array.isArray(e.errors)) {
      const msgs = e.errors
        .map((x: any) => x?.defaultMessage || x?.message)
        .filter(Boolean);
      if (msgs.length) return msgs.join(' | ');
    }

    return this.extractMsg(err, fallback);
  }
}

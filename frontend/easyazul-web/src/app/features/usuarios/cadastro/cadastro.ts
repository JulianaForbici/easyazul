import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UsuarioService, DadosCadastroUsuario, TipoUsuario } from '../../../core/api/usuario.service/usuario.service';
import { MaskInputDirective } from '../../../core/shared/masks/mask-input.directive';

type AbaCadastro = 'PF' | 'PJ';

@Component({
  standalone: true,
  selector: 'app-cadastro',
  imports: [CommonModule, FormsModule, RouterLink, MaskInputDirective],
  templateUrl: './cadastro.html',
  styleUrl: './cadastro.scss',
})
export class CadastroComponent implements OnInit {
  carregando = false;
  erroGeral: string | null = null;

  aba: AbaCadastro = 'PF';

  // limites para data nascimento
  readonly hoje = this.formatDateInput(new Date());
  readonly minNascimento = '1900-01-01';

  readonly telefonePattern = '^\\(?\\d{2}\\)?[ ]?9?\\d{4}-?\\d{4}$';

  form: DadosCadastroUsuario = {
    nome: '',
    email: '',
    senha: '',
    telefone: '',
    tipo: 'MOTORISTA',
    cpf: '',
    cnpj: null,
    dataNascimento: null,
    razaoSocial: null,
  };

  constructor(private api: UsuarioService, private router: Router, private route: ActivatedRoute) {}

  ngOnInit(): void {
    const tipo = (this.route.snapshot.queryParamMap.get('tipo') || '').toUpperCase();
    if (tipo === 'PJ') {
      this.selecionarPJ();
    } else if (tipo === 'PF') {
      this.selecionarPF();
    }
  }

  selecionarPF(): void {
    this.aba = 'PF';
    this.form.tipo = 'MOTORISTA';
    this.form.cnpj = null;
    this.form.razaoSocial = null;
  }

  selecionarPJ(): void {
    this.aba = 'PJ';
    this.form.tipo = 'EMPRESA';
    this.form.cpf = null;
  }

  get isPF(): boolean {
    return this.aba === 'PF';
  }

  get isPJ(): boolean {
    return this.aba === 'PJ';
  }

  private cpfValidoBasico(cpf: string): boolean {
    const digits = (cpf || '').replace(/\D/g, '');
    return digits.length === 11;
  }

  private cnpjValidoBasico(cnpj: string): boolean {
    const digits = (cnpj || '').replace(/\D/g, '');
    return digits.length === 14;
  }

  private telefoneValidoBasico(tel: string): boolean {
    const r = new RegExp(this.telefonePattern);
    return r.test(tel || '');
  }

  cadastrar(f: NgForm): void {
    this.erroGeral = null;

    // validações front (evita 400)
    if (!this.telefoneValidoBasico(this.form.telefone)) {
      this.erroGeral = 'Telefone inválido.';
      return;
    }

    if (this.isPF && !this.cpfValidoBasico(String(this.form.cpf ?? ''))) {
      this.erroGeral = 'CPF inválido.';
      return;
    }

    if (this.isPJ && !this.cnpjValidoBasico(String(this.form.cnpj ?? ''))) {
      this.erroGeral = 'CNPJ inválido.';
      return;
    }

    if (this.isPJ && !String(this.form.razaoSocial ?? '').trim()) {
      this.erroGeral = 'Razão social é obrigatória para pessoa jurídica.';
      return;
    }

    if (f.invalid) {
      this.erroGeral = 'Verifique os campos do formulário.';
      return;
    }

    this.carregando = true;

    const onlyDigits = (v: string | null | undefined) => (v ?? '').replace(/\D/g, '') || null;

    const payload: DadosCadastroUsuario = {
      nome: this.form.nome.trim(),
      email: this.form.email.trim(),
      senha: this.form.senha,
      telefone: (this.form.telefone ?? '').replace(/\D/g, ''),
      tipo: this.form.tipo as TipoUsuario,
      dataNascimento: this.form.dataNascimento || null,
      cpf: this.isPF ? onlyDigits(this.form.cpf as any) : null,
      cnpj: this.isPJ ? onlyDigits(this.form.cnpj as any) : null,
      razaoSocial: this.isPJ ? (String(this.form.razaoSocial ?? '').trim() || null) : null,
    };

    this.api.cadastrar(payload).subscribe({
      next: () => this.router.navigateByUrl('/login'),
      error: (err: unknown) => {
        console.error(err);
        this.erroGeral = 'Não foi possível cadastrar. Verifique CPF/CNPJ/Telefone.';
        this.carregando = false;
      },
      complete: () => (this.carregando = false),
    });
  }

  private formatDateInput(d: Date): string {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
}

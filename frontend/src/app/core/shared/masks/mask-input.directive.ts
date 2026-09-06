import { Directive, ElementRef, HostListener, Input } from '@angular/core';

type MaskKind = 'cpf' | 'cnpj' | 'telefone';

@Directive({
  standalone: true,
  selector: '[appMaskInput]',
})
export class MaskInputDirective {
  @Input('appMaskInput') kind: MaskKind = 'telefone';

  constructor(private el: ElementRef<HTMLInputElement>) {}

  @HostListener('input')
  onInput(): void {
    const input = this.el.nativeElement;
    const raw = input.value || '';
    const digits = raw.replace(/\D/g, '');

    let masked = digits;

    if (this.kind === 'cpf') masked = this.maskCpf(digits);
    if (this.kind === 'cnpj') masked = this.maskCnpj(digits);
    if (this.kind === 'telefone') masked = this.maskTelefone(digits);

    if (input.value !== masked) {
      input.value = masked;
    }
  }

  private maskCpf(d: string): string {
    const v = d.slice(0, 11);
    const a = v.slice(0, 3);
    const b = v.slice(3, 6);
    const c = v.slice(6, 9);
    const e = v.slice(9, 11);

    let out = a;
    if (b) out += '.' + b;
    if (c) out += '.' + c;
    if (e) out += '-' + e;
    return out;
  }

  private maskCnpj(d: string): string {
    const v = d.slice(0, 14);
    const a = v.slice(0, 2);
    const b = v.slice(2, 5);
    const c = v.slice(5, 8);
    const d1 = v.slice(8, 12);
    const e = v.slice(12, 14);

    let out = a;
    if (b) out += '.' + b;
    if (c) out += '.' + c;
    if (d1) out += '/' + d1;
    if (e) out += '-' + e;
    return out;
  }

  private maskTelefone(d: string): string {
    const v = d.slice(0, 11);
    const ddd = v.slice(0, 2);
    const rest = v.slice(2);

    let out = '';
    if (ddd) out = `(${ddd}) `;

    if (rest.length >= 9) {
      const p1 = rest.slice(0, 5);
      const p2 = rest.slice(5, 9);
      out += p2 ? `${p1}-${p2}` : p1;
      return out;
    }

    if (rest.length >= 8) {
      const p1 = rest.slice(0, 4);
      const p2 = rest.slice(4, 8);
      out += p2 ? `${p1}-${p2}` : p1;
      return out;
    }

    out += rest;
    return out;
  }
}

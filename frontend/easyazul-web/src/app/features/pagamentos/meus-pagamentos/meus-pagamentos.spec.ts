import { TestBed } from '@angular/core/testing';
import { MeusPagamentosComponent } from './meus-pagamentos';

describe('MeusPagamentosComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MeusPagamentosComponent],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(MeusPagamentosComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});

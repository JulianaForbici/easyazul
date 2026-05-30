import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PagamentosAdminListComponent } from './pagamentos-admin-list';

describe('PagamentosAdminListComponent', () => {
  let component: PagamentosAdminListComponent;
  let fixture: ComponentFixture<PagamentosAdminListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PagamentosAdminListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PagamentosAdminListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    });
});

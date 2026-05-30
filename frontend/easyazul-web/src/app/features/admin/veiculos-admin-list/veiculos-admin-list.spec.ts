import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VeiculosAdminListComponent } from './veiculos-admin-list';

describe('VeiculosAdminListComponent', () => {
  let component: VeiculosAdminListComponent;
  let fixture: ComponentFixture<VeiculosAdminListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VeiculosAdminListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VeiculosAdminListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

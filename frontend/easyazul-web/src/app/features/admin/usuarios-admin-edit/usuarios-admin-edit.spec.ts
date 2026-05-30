import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UsuariosAdminEditComponent } from './usuarios-admin-edit';

describe('UsuariosAdminEditComponent', () => {
  let component: UsuariosAdminEditComponent;
  let fixture: ComponentFixture<UsuariosAdminEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuariosAdminEditComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(UsuariosAdminEditComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

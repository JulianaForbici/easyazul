import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ZonasAdminForm } from './zonas-admin-form';

describe('ZonasAdminForm', () => {
  let component: ZonasAdminForm;
  let fixture: ComponentFixture<ZonasAdminForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ZonasAdminForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ZonasAdminForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

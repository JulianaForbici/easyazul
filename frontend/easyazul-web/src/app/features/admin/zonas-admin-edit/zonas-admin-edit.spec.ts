import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ZonasAdminEdit } from './zonas-admin-edit';

describe('ZonasAdminEdit', () => {
  let component: ZonasAdminEdit;
  let fixture: ComponentFixture<ZonasAdminEdit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ZonasAdminEdit]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ZonasAdminEdit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

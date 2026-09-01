import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ZonasAdminEditComponent } from './zonas-admin-edit';

describe('ZonasAdminEditComponent', () => {
  let component: ZonasAdminEditComponent;
  let fixture: ComponentFixture<ZonasAdminEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ZonasAdminEditComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ZonasAdminEditComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

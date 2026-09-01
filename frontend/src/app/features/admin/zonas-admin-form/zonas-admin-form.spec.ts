import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ZonasAdminFormComponent } from './zonas-admin-form';

describe('ZonasAdminFormComponent', () => {
  let component: ZonasAdminFormComponent;
  let fixture: ComponentFixture<ZonasAdminFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ZonasAdminFormComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ZonasAdminFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

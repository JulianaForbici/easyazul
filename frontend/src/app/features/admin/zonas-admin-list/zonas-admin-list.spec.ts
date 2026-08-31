import { ComponentFixture, TestBed } from '@angular/core/testing';
import {ZonasAdminEdit} from '../zonas-admin-edit/zonas-admin-edit';
import {ZonasAdminListComponent} from './zonas-admin-list';

describe('ZonasAdminListComponent', () => {
  let component: ZonasAdminListComponent;
  let fixture: ComponentFixture<ZonasAdminListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ZonasAdminListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ZonasAdminListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { TicketsAdminListComponent } from './tickets-admin-list';

describe('TicketsAdminListComponent', () => {
  let component: TicketsAdminListComponent;
  let fixture: ComponentFixture<TicketsAdminListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketsAdminListComponent, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(TicketsAdminListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

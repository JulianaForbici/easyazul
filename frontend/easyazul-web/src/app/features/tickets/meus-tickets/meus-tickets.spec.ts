import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { MeusTicketsComponent } from './meus-tickets';

describe('MeusTicketsComponent', () => {
  let component: MeusTicketsComponent;
  let fixture: ComponentFixture<MeusTicketsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MeusTicketsComponent, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(MeusTicketsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { VeiculosFormComponent } from './veiculos-form';

describe('VeiculosFormComponent', () => {
  let component: VeiculosFormComponent;
  let fixture: ComponentFixture<VeiculosFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VeiculosFormComponent,
        HttpClientTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VeiculosFormComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('aberto', false);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

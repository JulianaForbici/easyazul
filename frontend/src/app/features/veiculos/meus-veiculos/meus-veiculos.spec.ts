import { ComponentFixture, TestBed } from '@angular/core/testing';
import {VeiculosFormComponent} from '../veiculos-form/veiculos-form';



describe('VeiculosFormComponent', () => {
  let component: VeiculosFormComponent;
  let fixture: ComponentFixture<VeiculosFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VeiculosFormComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(VeiculosFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

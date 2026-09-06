import { Component } from '@angular/core';
import { ActivatedRouteSnapshot, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './core/layout/header/header';
import { FooterComponent } from './core/layout/footer/footer';
import { TicketExpiryAlertComponent } from './shared/tickets/ticket-expiry-alert/ticket-expiry-alert';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, FooterComponent, TicketExpiryAlertComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class AppComponent {
  hideHeader$!: Observable<boolean>;

  constructor(private router: Router) {
    this.hideHeader$ = this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        const root = this.router.routerState.snapshot.root;
        const leaf = this.getLeaf(root);
        return leaf?.data?.['hideHeader'] === true;
      })
    );
  }

  private getLeaf(route: ActivatedRouteSnapshot): ActivatedRouteSnapshot {
    let cur = route;
    while (cur.firstChild) cur = cur.firstChild;
    return cur;
  }
}

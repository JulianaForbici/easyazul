import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin.guard';
import {VeiculosAdminListComponent} from './features/admin/veiculos-admin-list/veiculos-admin-list';
import {ZonasAdminListComponent} from './features/admin/zonas-admin-list/zonas-admin-list';
import {authGuard} from './core/auth/auth.guard';

// (se tiver authGuard, recomendo usar em /veiculos)
// import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },

  {
    path: 'home',
    data: { hideHeader: true },
    loadComponent: () => import('./features/public/home/home').then(m => m.HomeComponent),
  },

  {
    path: 'login',
    data: { hideHeader: true },
    loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent),
  },

  {
    path: 'cadastro',
    data: { hideHeader: true },
    loadComponent: () => import('./features/usuarios/cadastro/cadastro').then(m => m.CadastroComponent),
  },

  {
    path: 'perfil',
    loadComponent: () => import('./features/usuarios/perfil/perfil').then(m => m.PerfilComponent),
  },
  {
    path: 'veiculos',
    canActivate: [authGuard],
    loadComponent: () => import('./features/veiculos/meus-veiculos/meus-veiculos')
      .then(m => m.MeusVeiculosComponent),
  },

  {
    path: 'tickets',
    canActivate: [authGuard],
    loadComponent: () => import('./features/tickets/meus-tickets/meus-tickets')
      .then(m => m.MeusTicketsComponent),
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      {
        path: 'usuarios/novo',
        loadComponent: () => import('./features/admin/usuarios-admin-form/usuarios-admin-form')
          .then(m => m.UsuariosAdminFormComponent),
      },
      {
        path: 'usuarios/:id/editar',
        loadComponent: () => import('./features/admin/usuarios-admin-edit/usuarios-admin-edit')
          .then(m => m.UsuariosAdminEditComponent),
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./features/admin/usuarios-admin-list/usuarios-admin-list')
          .then(m => m.UsuariosAdminListComponent),
      },
      {
        path: 'veiculos',
        loadComponent: () => import('./features/admin/veiculos-admin-list/veiculos-admin-list')
          .then(m => m.VeiculosAdminListComponent),
      },

      {
        path: 'zonas',
        loadComponent: () => import('./features/admin/zonas-admin-list/zonas-admin-list')
          .then(m => m.ZonasAdminListComponent),
      },

      {
        path: 'zonas/novo',
        loadComponent: () =>
          import('./features/admin/zonas-admin-form/zonas-admin-form').then(m => m.ZonasAdminFormComponent),
      },

      {
        path: 'zonas/:id/editar',
        loadComponent: () => import('./features/admin/zonas-admin-edit/zonas-admin-edit').then(m => m.ZonasAdminEditComponent),
      },

      {
        path: 'tickets',
        loadComponent: () => import('./features/admin/tickets-admin-list/tickets-admin-list')
          .then(m => m.TicketsAdminListComponent),
      },

      {
        path: 'pagamentos',
        loadComponent: () => import('./features/admin/pagamentos-admin-list/pagamentos-admin-list')
          .then(m => m.PagamentosAdminListComponent),
      },

      { path: '', pathMatch: 'full', redirectTo: 'usuarios' },
    ],
  },

  {
    path: 'mapa',
    loadComponent: () => import('./features/mapa/mapa').then(m => m.MapaComponent),
  },

  {
    path: 'pagamentos',
    loadComponent: () =>
      import('./features/pagamentos/meus-pagamentos/meus-pagamentos').then(m => m.MeusPagamentosComponent),
  },

  { path: '**', redirectTo: 'home' },
];

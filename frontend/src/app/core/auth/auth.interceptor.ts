import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/login')) return next(req);

  const raw = localStorage.getItem('easyazul_auth');
  if (!raw) return next(req);

  try {
    const data = JSON.parse(raw) as { token?: string };
    const token = data?.token;
    if (!token) return next(req);

    return next(
      req.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
      })
    );
  } catch {
    return next(req);
  }
};

export type TipoUsuario = 'MOTORISTA' | 'EMPRESA' | 'ADMINISTRADOR' | 'FISCAL';

export type LoginResponse = {
  token: string;
  id: number;
  nome: string;
  tipo: TipoUsuario;
};

export type AuthUser = {
  token: string;
  idUsuario: number;
  nome: string;
  tipo: TipoUsuario;
};

export type LoginRequest = { email: string; senha: string; };

export interface User {
  id: number;
  username: string;
  avatar?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
}
export interface Taikhoan {
  maTk: number; // Tương ứng với MA_TK
  tenDn: string; // Tương ứng với TEN_DN
  mk?: string; // Tương ứng với MK, optional vì không cần gửi password lên frontend
  avarta?: string; // Tương ứng với AVARTA
}

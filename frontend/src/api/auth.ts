import type { User } from '../types';

const BASE = '/api';

export async function fetchCurrentUser(): Promise<User | null> {
  const res = await fetch(`${BASE}/me`);

  if (res.status === 401 || res.status === 403) {
    return null;
  }

  if (!res.ok) {
    throw new Error('Failed to load current user');
  }

  return res.json();
}

export function login() {
  window.location.href = '/oauth2/authorization/github';
}

export function logout() {
  window.location.href = '/logout';
}

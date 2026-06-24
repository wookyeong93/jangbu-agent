import type { UpdateProfileRequest, UserResponse } from '~/types/auth'

/** UserController 대응 (/api/users/**). */

export function getProfile(): Promise<UserResponse> {
  return useApiFetch<UserResponse>('/api/users/me')
}

export function updateProfile(payload: UpdateProfileRequest): Promise<void> {
  return useApiFetch<void>('/api/users/me', { method: 'PATCH', body: payload })
}

/** AuthController.login 요청과 1:1 대응 (LoginRequest.java). */
export interface LoginRequest {
  userId: string
  password: string
}

/** AuthController.login/reissue 응답과 1:1 대응 (TokenResponse.java). */
export interface TokenResponse {
  accessToken: string
}

/** UserController.getProfile 응답과 1:1 대응 (UserResponse.java). */
export interface UserResponse {
  userId: string
  userNm: string
}

/** UserController.updateProfile 요청과 1:1 대응 (UpdateProfileRequest.java). */
export interface UpdateProfileRequest {
  userNm?: string
  currentPassword?: string
  newPassword?: string
}

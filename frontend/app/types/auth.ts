/** AuthController.login/reissue 응답과 1:1 대응 (TokenResponse.java). */
export interface TokenResponse {
  accessToken: string
}

/** UserController.getProfile 응답과 1:1 대응 (UserResponse.java). */
export interface UserResponse {
  userId: string
  userNm: string
}

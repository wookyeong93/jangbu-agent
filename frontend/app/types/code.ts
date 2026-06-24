/** 백엔드 domain/code 응답 DTO와 1:1 대응 (CodeResponse.java / GroupCodeResponse.java). */
export interface CodeResponse {
  code: string
  codeName: string
  sortOrder: number
}

export interface GroupCodeResponse {
  groupCode: string
  groupName: string
  codes: CodeResponse[]
}

/** SelectBox/RadioGroup 등 UI 컴포넌트가 받는 옵션 형태. CodeResponse에서 변환해 사용. */
export interface SelectOption {
  value: string
  label: string
}

export function codeToOption(code: CodeResponse): SelectOption {
  return { value: code.code, label: code.codeName }
}

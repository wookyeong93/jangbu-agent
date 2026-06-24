import type { DashboardResponse } from '~/types/dashboard'

/** DashboardController 대응 (/api/dashboard). 둘 다 생략하면 백엔드 기본값(오늘이 속한 달, 6개월)을 따른다. */

export interface GetDashboardParams {
  /** "yyyy-MM" 형식. 생략하면 오늘이 속한 달. */
  month?: string
  /** 1~12. 생략하면 6. */
  months?: number
}

export function getDashboard(params: GetDashboardParams = {}): Promise<DashboardResponse> {
  const query: Record<string, unknown> = {}
  if (params.month) {
    query.month = params.month
  }
  if (params.months) {
    query.months = params.months
  }
  return useApiFetch<DashboardResponse>('/api/dashboard', {
    query: Object.keys(query).length > 0 ? query : undefined
  })
}

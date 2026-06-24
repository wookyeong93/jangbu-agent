import type { GuideResponse } from '~/types/guide'

/** GuideController 대응 (/api/guide). */

export function getDailyGuide(): Promise<GuideResponse> {
  return useApiFetch<GuideResponse>('/api/guide/daily')
}

export interface NavItem {
  label: string
  to: string
  icon: string
}

export const NAV_ITEMS: NavItem[] = [
  { label: '대시보드', to: '/dashboard', icon: '📊' },
  { label: '장부', to: '/ledger', icon: '📒' },
  { label: 'AI 가이드', to: '/guide', icon: '🤖' }
]

/** DashboardController.getDashboard 응답과 1:1 대응 (MonthlyKpiDto.java). */
export interface MonthlyKpi {
  year: number
  month: number
  totalPurchase: number
  totalSale: number
  totalExpense: number
  netProfit: number
  marginRate: number
}

/** DashboardController.getDashboard 응답과 1:1 대응 (DashboardResponse.java). */
export interface DashboardResponse {
  current: MonthlyKpi
  trend: MonthlyKpi[]
}

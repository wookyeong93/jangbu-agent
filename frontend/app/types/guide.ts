/** GuideController.getDailyGuide 응답과 1:1 대응 (GuideResponse.java). */
export interface GuideResponse {
  guideDt: string
  guideText: string
  basedPurchase: number
  basedSale: number
  basedExpense: number
  basedProfit: number
  marginRate: number | null
  modelName: string | null
}

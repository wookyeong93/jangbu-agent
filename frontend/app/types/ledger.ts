/** LedgerController 응답과 1:1 대응 (LedgerResponse.java). */
export interface LedgerItem {
  ledgerNo: number
  trxType: string
  trxDate: string
  trxName: string | null
  amount: number
  createdAt: string
  updatedAt: string | null
}

/** LedgerController.findMonthly 응답과 1:1 대응 (LedgerMonthlyResponse.java). */
export interface LedgerMonthlyResponse {
  year: number
  month: number
  items: LedgerItem[]
  totalPurchase: number
  totalSale: number
  totalExpense: number
  netProfit: number
  marginRate: number
}

/** LedgerController.create 요청과 1:1 대응 (LedgerCreateRequest.java). */
export interface CreateLedgerRequest {
  trxType: string
  amount: number
  trxDate?: string
  trxName?: string
}

/** LedgerController.update 요청과 1:1 대응 (LedgerUpdateRequest.java). trxDate는 create와 달리 필수. */
export interface UpdateLedgerRequest {
  trxType: string
  trxDate: string
  amount: number
  trxName?: string
}

import type { CreateLedgerRequest, LedgerItem, LedgerMonthlyResponse, UpdateLedgerRequest } from '~/types/ledger'

/** LedgerController 대응 (/api/ledger). */

export function getMonthlyLedger(year: number, month: number): Promise<LedgerMonthlyResponse> {
  return useApiFetch<LedgerMonthlyResponse>('/api/ledger', {
    query: { year, month }
  })
}

export function createLedger(payload: CreateLedgerRequest): Promise<LedgerItem> {
  return useApiFetch<LedgerItem>('/api/ledger', {
    method: 'POST',
    body: payload
  })
}

export function updateLedger(ledgerNo: number, payload: UpdateLedgerRequest): Promise<LedgerItem> {
  return useApiFetch<LedgerItem>(`/api/ledger/${ledgerNo}`, {
    method: 'PUT',
    body: payload
  })
}

export function deleteLedger(ledgerNo: number): Promise<void> {
  return useApiFetch<void>(`/api/ledger/${ledgerNo}`, {
    method: 'DELETE'
  })
}

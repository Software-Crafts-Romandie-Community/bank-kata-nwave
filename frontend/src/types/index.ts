export interface BalanceResponse {
  balance: number
}

export interface ApiError extends Error {
  status: number
  detail: string
  available?: number
}

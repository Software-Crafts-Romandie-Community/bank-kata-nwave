import type { BalanceResponse, ApiError } from '../types'

export interface TransactionDto {
  type: string // "DEPOSIT" or "WITHDRAWAL"
  amount: number
  date: string // ISO 8601 string e.g. "2026-06-18T14:32:00Z"
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    const error = new Error(body.detail ?? 'API error') as Error & ApiError
    error.status = response.status
    error.detail = body.detail ?? 'API error'
    error.available = body.available
    throw error
  }
  return response.json() as Promise<T>
}

export async function getBalance(): Promise<BalanceResponse> {
  const response = await fetch('/api/balance')
  return handleResponse<BalanceResponse>(response)
}

export async function deposit(amount: number): Promise<BalanceResponse> {
  const response = await fetch('/api/deposit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ amount }),
  })
  return handleResponse<BalanceResponse>(response)
}

export async function withdraw(amount: number): Promise<BalanceResponse> {
  const response = await fetch('/api/withdraw', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ amount }),
  })
  return handleResponse<BalanceResponse>(response)
}

export async function getStatement(): Promise<TransactionDto[]> {
  const response = await fetch('/api/statement')
  return handleResponse<TransactionDto[]>(response)
}

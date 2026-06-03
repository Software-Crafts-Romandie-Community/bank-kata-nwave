import type { BalanceResponse, ApiError } from '../types'

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

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as bankApi from './api/bankApi'
import App from './App'

vi.mock('./api/bankApi')
const mockApi = vi.mocked(bankApi)

beforeEach(() => {
  vi.clearAllMocks()
})

describe('App', () => {
  it('loads and displays the balance on mount', async () => {
    mockApi.getBalance.mockResolvedValue({ balance: 250.5 })
    render(<App />)

    await waitFor(() => {
      expect(screen.getByText(/250/)).toBeInTheDocument()
    })
    expect(mockApi.getBalance).toHaveBeenCalledTimes(1)
  })

  it('shows error when balance cannot be loaded', async () => {
    mockApi.getBalance.mockRejectedValue(new Error('Network error'))
    render(<App />)

    await waitFor(() => {
      expect(screen.getByText('Impossible de charger le solde.')).toBeInTheDocument()
    })
  })

  it('updates balance after successful deposit', async () => {
    const user = userEvent.setup()
    mockApi.getBalance.mockResolvedValue({ balance: 100.0 })
    mockApi.deposit.mockResolvedValue({ balance: 250.0 })
    render(<App />)

    await waitFor(() => expect(screen.getByText(/100/)).toBeInTheDocument())

    await user.type(screen.getByRole('spinbutton'), '150')
    await user.click(screen.getByRole('button', { name: /déposer/i }))

    await waitFor(() => expect(screen.getByText(/250/)).toBeInTheDocument())
  })

  it('shows insufficient funds error on 409 response', async () => {
    const user = userEvent.setup()
    mockApi.getBalance.mockResolvedValue({ balance: 70.5 })
    const apiError = new Error('Insufficient funds') as Error & { status?: number; available?: number }
    apiError.status = 409
    apiError.available = 70.5
    mockApi.withdraw.mockRejectedValue(apiError)
    render(<App />)

    await waitFor(() => expect(screen.getByText(/70/)).toBeInTheDocument())

    await user.type(screen.getByRole('spinbutton'), '100')
    await user.click(screen.getByRole('button', { name: /retirer/i }))

    await waitFor(() => {
      expect(screen.getByText(/Fonds insuffisants/)).toBeInTheDocument()
    })
  })
})

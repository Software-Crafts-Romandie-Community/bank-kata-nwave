import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import StatementView from './StatementView'
import type { TransactionDto } from './api/bankApi'

describe('StatementView', () => {
  const transactions: TransactionDto[] = [
    { type: 'DEPOSIT', amount: 100, date: '2026-06-18T10:00:00Z' },
    { type: 'WITHDRAWAL', amount: 50, date: '2026-06-18T11:00:00Z' },
  ]

  it('renders table headers: Date, Type, Montant', () => {
    render(<StatementView transactions={transactions} balance={50} onBack={vi.fn()} />)

    expect(screen.getByRole('columnheader', { name: /date/i })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: /type/i })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: /montant/i })).toBeInTheDocument()
  })

  it('renders one row per transaction with type label and signed amount', () => {
    render(<StatementView transactions={transactions} balance={50} onBack={vi.fn()} />)

    expect(screen.getByText('Dépôt')).toBeInTheDocument()
    expect(screen.getByText('+100')).toBeInTheDocument()
    expect(screen.getByText('Retrait')).toBeInTheDocument()
    expect(screen.getByText('-50')).toBeInTheDocument()
  })

  it('shows empty message when no transactions', () => {
    render(<StatementView transactions={[]} balance={0} onBack={vi.fn()} />)

    expect(
      screen.getByText('Aucune transaction enregistrée dans cette session.'),
    ).toBeInTheDocument()
  })

  it('displays current balance', () => {
    render(<StatementView transactions={transactions} balance={50} onBack={vi.fn()} />)

    expect(screen.getByText(/Solde actuel/)).toBeInTheDocument()
  })

  it('calls onBack when Retour button is clicked', async () => {
    const user = userEvent.setup()
    const onBack = vi.fn()
    render(<StatementView transactions={transactions} balance={50} onBack={onBack} />)

    await user.click(screen.getByRole('button', { name: /retour/i }))

    expect(onBack).toHaveBeenCalledTimes(1)
  })
})

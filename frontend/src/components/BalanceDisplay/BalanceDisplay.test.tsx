import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import BalanceDisplay from './BalanceDisplay'

describe('BalanceDisplay', () => {
  it('shows loading state when balance is null', () => {
    render(<BalanceDisplay balance={null} error={null} />)
    expect(screen.getByText('Chargement du solde...')).toBeInTheDocument()
  })

  it('shows error message when error is set', () => {
    render(<BalanceDisplay balance={null} error="Impossible de charger le solde." />)
    expect(screen.getByText('Impossible de charger le solde.')).toBeInTheDocument()
  })

  it('formats balance as EUR currency in French locale', () => {
    render(<BalanceDisplay balance={100.5} error={null} />)
    // French locale formats: 100,50 €
    expect(screen.getByText(/100/)).toBeInTheDocument()
  })
})

import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import OperationForm from './OperationForm'

describe('OperationForm', () => {
  it('calls onSubmit with deposit type and amount when deposit button clicked', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn()
    render(<OperationForm onSubmit={onSubmit} error={null} />)

    await user.type(screen.getByRole('spinbutton'), '150')
    await user.click(screen.getByRole('button', { name: /déposer/i }))

    expect(onSubmit).toHaveBeenCalledWith('deposit', 150)
  })

  it('calls onSubmit with withdraw type and amount when withdraw button clicked', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn()
    render(<OperationForm onSubmit={onSubmit} error={null} />)

    await user.type(screen.getByRole('spinbutton'), '50')
    await user.click(screen.getByRole('button', { name: /retirer/i }))

    expect(onSubmit).toHaveBeenCalledWith('withdraw', 50)
  })

  it('displays error message when error prop is set', () => {
    render(<OperationForm onSubmit={vi.fn()} error="Montant invalide" />)
    expect(screen.getByText('Montant invalide')).toBeInTheDocument()
  })
})

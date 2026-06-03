import { useState } from 'react'

type OperationType = 'deposit' | 'withdraw'

interface OperationFormProps {
  onSubmit: (type: OperationType, amount: number) => void
  error: string | null
}

export default function OperationForm({ onSubmit, error }: OperationFormProps) {
  const [amount, setAmount] = useState('')

  function handleSubmit(type: OperationType) {
    const parsed = parseFloat(amount)
    if (!isNaN(parsed) && parsed > 0) {
      onSubmit(type, parsed)
      setAmount('')
    }
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'Enter') handleSubmit('deposit')
  }

  return (
    <div className="operation-form">
      <div className="operation-form__field">
        <label className="operation-form__label" htmlFor="amount-input">
          Montant
        </label>
        <input
          id="amount-input"
          className="operation-form__input"
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          onKeyDown={handleKeyDown}
          min="0.01"
          step="0.01"
          placeholder="0,00"
          autoComplete="off"
        />
        <span className="operation-form__currency" aria-hidden="true">EUR</span>
      </div>

      <div className="operation-form__actions">
        <button
          className="btn btn--deposit"
          onClick={() => handleSubmit('deposit')}
        >
          Déposer
        </button>
        <button
          className="btn btn--withdraw"
          onClick={() => handleSubmit('withdraw')}
        >
          Retirer
        </button>
      </div>

      {error !== null && <p className="error-message">{error}</p>}
    </div>
  )
}

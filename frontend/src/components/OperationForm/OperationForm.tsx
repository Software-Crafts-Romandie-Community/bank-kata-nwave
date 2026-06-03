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
    if (!isNaN(parsed)) {
      onSubmit(type, parsed)
      setAmount('')
    }
  }

  return (
    <div className="operation-form">
      <input
        type="number"
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
        min="0"
        step="0.01"
        placeholder="Montant"
      />
      <button onClick={() => handleSubmit('deposit')}>Déposer</button>
      <button onClick={() => handleSubmit('withdraw')}>Retirer</button>
      {error !== null && <p className="error">{error}</p>}
    </div>
  )
}

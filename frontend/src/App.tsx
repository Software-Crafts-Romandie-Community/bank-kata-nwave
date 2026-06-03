import { useEffect, useState } from 'react'
import * as bankApi from './api/bankApi'
import type { ApiError } from './types'
import BalanceDisplay from './components/BalanceDisplay/BalanceDisplay'
import OperationForm from './components/OperationForm/OperationForm'

type OperationType = 'deposit' | 'withdraw'

function formatEur(amount: number): string {
  return amount.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' })
}

function resolveErrorMessage(err: unknown, type: OperationType): string {
  const apiErr = err as Partial<ApiError>
  if (apiErr.status === 409 && apiErr.available !== undefined) {
    return `Fonds insuffisants — solde disponible : ${formatEur(apiErr.available)}`
  }
  if (apiErr.status === 400) {
    return 'Montant invalide'
  }
  if (type === 'deposit') {
    return 'Erreur lors du dépôt.'
  }
  return 'Erreur lors du retrait.'
}

export default function App() {
  const [balance, setBalance] = useState<number | null>(null)
  const [balanceError, setBalanceError] = useState<string | null>(null)
  const [operationError, setOperationError] = useState<string | null>(null)

  useEffect(() => {
    bankApi
      .getBalance()
      .then((data) => setBalance(data.balance))
      .catch(() => setBalanceError('Impossible de charger le solde.'))
  }, [])

  async function handleOperation(type: OperationType, amount: number) {
    setOperationError(null)
    try {
      const operation = type === 'deposit' ? bankApi.deposit : bankApi.withdraw
      const data = await operation(amount)
      setBalance(data.balance)
    } catch (err) {
      setOperationError(resolveErrorMessage(err, type))
    }
  }

  return (
    <main>
      <h1>Bank Application</h1>
      <BalanceDisplay balance={balance} error={balanceError} />
      <OperationForm onSubmit={handleOperation} error={operationError} />
    </main>
  )
}

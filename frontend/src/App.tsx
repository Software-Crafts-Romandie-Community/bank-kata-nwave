import { useEffect, useState } from 'react'
import * as bankApi from './api/bankApi'
import type { TransactionDto } from './api/bankApi'
import type { ApiError } from './types'
import BalanceDisplay from './components/BalanceDisplay/BalanceDisplay'
import OperationForm from './components/OperationForm/OperationForm'
import StatementView from './StatementView'

type OperationType = 'deposit' | 'withdraw'

function formatEur(amount: number): string {
  return amount.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' })
}

function resolveErrorMessage(err: unknown, type: OperationType): string {
  const apiErr = err as Partial<ApiError>
  if (apiErr.status === 409 && apiErr.available !== undefined) {
    return `Fonds insuffisants — solde disponible : ${formatEur(apiErr.available)}`
  }
  if (apiErr.status === 400) return 'Montant invalide'
  if (type === 'deposit') return 'Erreur lors du dépôt.'
  return 'Erreur lors du retrait.'
}

export default function App() {
  const [balance, setBalance] = useState<number | null>(null)
  const [balanceError, setBalanceError] = useState<string | null>(null)
  const [operationError, setOperationError] = useState<string | null>(null)
  const [showStatement, setShowStatement] = useState(false)
  const [transactions, setTransactions] = useState<TransactionDto[]>([])

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

  async function loadStatement() {
    const data = await bankApi.getStatement()
    setTransactions(data)
    setShowStatement(true)
  }

  if (showStatement) {
    return (
      <StatementView
        transactions={transactions}
        balance={balance ?? 0}
        onBack={() => setShowStatement(false)}
      />
    )
  }

  return (
    <div className="app">
      <header className="app-header">
        <span className="app-header__wordmark">Banque</span>
        <span className="app-header__subtitle">Tableau de bord</span>
      </header>

      <main className="app-body">
        <BalanceDisplay balance={balance} error={balanceError} />

        <div className="section-divider" aria-hidden="true">Opération</div>

        <OperationForm onSubmit={handleOperation} error={operationError} />

        <button
          type="button"
          aria-label="Voir le relevé de compte"
          onClick={loadStatement}
        >
          Relevé
        </button>
      </main>

      <footer className="app-footer">
        <span className="app-footer__note">Transactions sécurisées</span>
        <span className="app-footer__note">
          {new Date().toLocaleDateString('fr-FR', {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
          })}
        </span>
      </footer>
    </div>
  )
}

import type { TransactionDto } from './api/bankApi'

interface StatementViewProps {
  transactions: TransactionDto[]
  balance: number
  onBack: () => void
}

function formatType(type: string): string {
  if (type === 'DEPOSIT') return 'Dépôt'
  if (type === 'WITHDRAWAL') return 'Retrait'
  return type
}

function formatAmount(type: string, amount: number): string {
  if (type === 'DEPOSIT') return `+${amount}`
  if (type === 'WITHDRAWAL') return `-${amount}`
  return String(amount)
}

export default function StatementView({ transactions, balance, onBack }: StatementViewProps) {
  return (
    <div className="statement-view">
      <div className="statement-view__actions">
        <button type="button" onClick={onBack}>
          Retour
        </button>
      </div>

      {transactions.length === 0 ? (
        <p>Aucune transaction enregistrée dans cette session.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th scope="col">Date</th>
              <th scope="col">Type</th>
              <th scope="col">Montant</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((t, index) => (
              <tr key={`${t.type}-${t.date}-${index}`}>
                <td>{new Date(t.date).toLocaleString()}</td>
                <td>{formatType(t.type)}</td>
                <td>{formatAmount(t.type, t.amount)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div className="statement-view__balance">
        <span>Solde actuel : {balance}</span>
      </div>
    </div>
  )
}

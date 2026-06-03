interface BalanceDisplayProps {
  balance: number | null
  error: string | null
}

function formatEur(amount: number): string {
  return amount.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' })
}

export default function BalanceDisplay({ balance, error }: BalanceDisplayProps) {
  if (error !== null) {
    return <p className="error">{error}</p>
  }
  if (balance === null) {
    return <p>Chargement du solde...</p>
  }
  return (
    <div className="balance">
      <h2>Solde : {formatEur(balance)}</h2>
    </div>
  )
}

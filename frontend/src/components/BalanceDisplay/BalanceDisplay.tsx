interface BalanceDisplayProps {
  balance: number | null
  error: string | null
}

function formatAmount(amount: number): string {
  return amount.toLocaleString('fr-FR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

export default function BalanceDisplay({ balance, error }: BalanceDisplayProps) {
  if (error !== null) {
    return (
      <div className="balance">
        <p className="balance__label">Solde actuel</p>
        <p className="error-message">{error}</p>
      </div>
    )
  }

  return (
    <div className="balance">
      <p className="balance__label">Solde actuel</p>
      <div className="balance__card">
        <div className="balance__card-inner">
          {balance === null ? (
            <span className="balance__loading">Chargement du solde...</span>
          ) : (
            <>
              <span className="balance__amount">{formatAmount(balance)}</span>
              <span className="balance__currency-tag">euros</span>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

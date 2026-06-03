const { useState, useEffect } = React;

function formatEur(amount) {
    return amount.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' });
}

function BalanceDisplay({ balance, loading }) {
    if (loading) {
        return <p>Chargement du solde...</p>;
    }
    if (balance === null) {
        return <p>Impossible de charger le solde.</p>;
    }
    return (
        <p><strong>Solde actuel : {formatEur(balance)}</strong></p>
    );
}

function OperationForm({ label, endpoint, onSuccess, onError }) {
    const [amount, setAmount] = useState('');
    const [submitting, setSubmitting] = useState(false);

    function handleSubmit(event) {
        event.preventDefault();
        const parsed = parseFloat(amount);
        if (!amount || isNaN(parsed)) {
            return;
        }
        setSubmitting(true);
        fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount: parsed }),
        })
            .then(function (response) {
                return response.json().then(function (data) {
                    return { status: response.status, data };
                });
            })
            .then(function ({ status, data }) {
                setSubmitting(false);
                if (status === 200) {
                    setAmount('');
                    onSuccess(data.balance);
                } else if (status === 409) {
                    const available = data.available !== undefined ? data.available : null;
                    onError('insufficient_funds', available);
                } else if (status === 400) {
                    onError('invalid_amount', null);
                } else {
                    onError('unknown', null);
                }
            })
            .catch(function () {
                setSubmitting(false);
                onError('unknown', null);
            });
    }

    return (
        <form onSubmit={handleSubmit}>
            <input
                type="number"
                step="0.01"
                min="0.01"
                placeholder="Montant"
                value={amount}
                onChange={function (e) { setAmount(e.target.value); }}
                disabled={submitting}
            />
            <button type="submit" disabled={submitting}>
                {label}
            </button>
        </form>
    );
}

function ErrorMessage({ errorType, available }) {
    if (!errorType) {
        return null;
    }
    if (errorType === 'insufficient_funds') {
        const detail = available !== null
            ? ' — solde disponible : ' + formatEur(available)
            : '';
        return <p className="message-error">Fonds insuffisants{detail}</p>;
    }
    if (errorType === 'invalid_amount') {
        return <p className="message-error">Montant invalide</p>;
    }
    return <p className="message-error">Une erreur est survenue.</p>;
}

function App() {
    const [balance, setBalance] = useState(null);
    const [loading, setLoading] = useState(true);
    const [depositError, setDepositError] = useState(null);
    const [depositErrorAvailable, setDepositErrorAvailable] = useState(null);
    const [withdrawError, setWithdrawError] = useState(null);
    const [withdrawErrorAvailable, setWithdrawErrorAvailable] = useState(null);

    useEffect(function () {
        fetch('/api/balance')
            .then(function (response) { return response.json(); })
            .then(function (data) {
                setBalance(data.balance);
                setLoading(false);
            })
            .catch(function () {
                setLoading(false);
            });
    }, []);

    function handleDepositSuccess(newBalance) {
        setBalance(newBalance);
        setDepositError(null);
        setDepositErrorAvailable(null);
    }

    function handleDepositError(errorType, available) {
        setDepositError(errorType);
        setDepositErrorAvailable(available);
    }

    function handleWithdrawSuccess(newBalance) {
        setBalance(newBalance);
        setWithdrawError(null);
        setWithdrawErrorAvailable(null);
    }

    function handleWithdrawError(errorType, available) {
        setWithdrawError(errorType);
        setWithdrawErrorAvailable(available);
    }

    return (
        <div>
            <h1>Bank Application</h1>

            <div className="balance-section">
                <h2>Solde</h2>
                <BalanceDisplay balance={balance} loading={loading} />
            </div>

            <div className="operation-section">
                <h2>Déposer</h2>
                <OperationForm
                    label="Déposer"
                    endpoint="/api/deposit"
                    onSuccess={handleDepositSuccess}
                    onError={handleDepositError}
                />
                <ErrorMessage errorType={depositError} available={depositErrorAvailable} />
            </div>

            <div className="operation-section">
                <h2>Retirer</h2>
                <OperationForm
                    label="Retirer"
                    endpoint="/api/withdraw"
                    onSuccess={handleWithdrawSuccess}
                    onError={handleWithdrawError}
                />
                <ErrorMessage errorType={withdrawError} available={withdrawErrorAvailable} />
            </div>
        </div>
    );
}

const container = document.getElementById('root');
const root = ReactDOM.createRoot(container);
root.render(React.createElement(App));

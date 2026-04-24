import { useState, useEffect, useCallback } from 'react';
import type { Expense } from '../types';
import { CATEGORIES } from '../types';
import { getExpenses, deleteExpense, getErrorMessage } from '../services/api';

interface Props {
  refreshTrigger: number;
}

const CATEGORY_COLORS: Record<string, string> = {
  'Food & Drink': '#085041',
  'Transport': '#0C447C',
  'Housing': '#3C3489',
  'Health': '#791F1F',
  'Shopping': '#72243E',
  'Entertainment': '#633806',
  'Education': '#27500A',
  'Utilities': '#444441',
  'Other': '#444441',
};

const CATEGORY_BG: Record<string, string> = {
  'Food & Drink': '#E1F5EE',
  'Transport': '#E6F1FB',
  'Housing': '#EEEDFE',
  'Health': '#FCEBEB',
  'Shopping': '#FBEAF0',
  'Entertainment': '#FAEEDA',
  'Education': '#EAF3DE',
  'Utilities': '#F1EFE8',
  'Other': '#F1EFE8',
};

function formatINR(amount: number): string {
  return '₹' + amount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(dateStr: string): string {
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
  });
}

export default function ExpenseList({ refreshTrigger }: Props) {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterCategory, setFilterCategory] = useState('');
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const fetchExpenses = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getExpenses(filterCategory || undefined);
      setExpenses(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [filterCategory]);

  useEffect(() => {
    fetchExpenses();
  }, [fetchExpenses, refreshTrigger]);

  async function handleDelete(id: number) {
    if (!confirm('Delete this expense?')) return;
    setDeletingId(id);
    try {
      await deleteExpense(id);
      setExpenses(prev => prev.filter(e => e.id !== id));
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setDeletingId(null);
    }
  }

  const total = expenses.reduce((sum, e) => sum + Number(e.amount), 0);

  return (
    <div className="expense-list-container">
      <div className="list-header">
        <h2 className="section-title">Expenses</h2>
        <select
          value={filterCategory}
          onChange={e => setFilterCategory(e.target.value)}
          aria-label="Filter by category"
          className="filter-select"
        >
          <option value="">All categories</option>
          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>

      <div className="total-bar" aria-live="polite">
        <span className="total-label">Total</span>
        <span className="total-amount">{formatINR(total)}</span>
        <span className="total-count">{expenses.length} {expenses.length === 1 ? 'expense' : 'expenses'}</span>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="state-msg">Loading expenses…</div>
      ) : expenses.length === 0 ? (
        <div className="state-msg empty">
          {filterCategory ? `No expenses in "${filterCategory}".` : 'No expenses yet. Add one!'}
        </div>
      ) : (
        <ul className="expense-ul" aria-label="Expense list">
          {expenses.map(expense => (
            <li key={expense.id} className="expense-card">
              <div className="exp-main">
                <span className="exp-desc" title={expense.description}>{expense.description}</span>
                <div className="exp-meta">
                  <span
                    className="badge"
                    style={{
                      background: CATEGORY_BG[expense.category] ?? '#F1EFE8',
                      color: CATEGORY_COLORS[expense.category] ?? '#444441',
                    }}
                  >
                    {expense.category}
                  </span>
                  <span className="exp-date">{formatDate(expense.date)}</span>
                </div>
              </div>
              <div className="exp-right">
                <span className="exp-amount">{formatINR(Number(expense.amount))}</span>
                <button
                  className="btn-delete"
                  onClick={() => handleDelete(expense.id)}
                  disabled={deletingId === expense.id}
                  aria-label={`Delete expense: ${expense.description}`}
                >
                  {deletingId === expense.id ? '…' : '×'}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

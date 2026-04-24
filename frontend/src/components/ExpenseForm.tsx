import { useState } from 'react';
import type { CreateExpenseRequest } from '../types';
import { CATEGORIES } from '../types';
import { createExpense, getErrorMessage } from '../services/api';

interface Props {
  onCreated: () => void;
}

interface FormErrors {
  amount?: string;
  category?: string;
  description?: string;
  date?: string;
  general?: string;
}

const today = new Date().toISOString().slice(0, 10);

export default function ExpenseForm({ onCreated }: Props) {
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState(today);
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  function validate(): boolean {
    const e: FormErrors = {};
    const num = parseFloat(amount);
    if (!amount || isNaN(num) || num <= 0) e.amount = 'Enter a positive amount.';
    if (!category) e.category = 'Select a category.';
    if (!description.trim()) e.description = 'Description is required.';
    if (!date) e.date = 'Pick a date.';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate() || submitting) return;

    setSubmitting(true);
    setErrors({});
    setSuccessMsg('');

    const request: CreateExpenseRequest = {
      amount: parseFloat(parseFloat(amount).toFixed(2)),
      category,
      description: description.trim(),
      date,
    };

    try {
      await createExpense(request);
      setAmount('');
      setCategory('');
      setDescription('');
      setDate(today);
      setSuccessMsg('Expense added!');
      setTimeout(() => setSuccessMsg(''), 3000);
      onCreated();
    } catch (err) {
      setErrors({ general: getErrorMessage(err) });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h2 className="section-title">Add Expense</h2>

      {successMsg && <div className="alert alert-success">{successMsg}</div>}
      {errors.general && <div className="alert alert-error">{errors.general}</div>}

      <div className="field">
        <label htmlFor="amount">Amount (₹)</label>
        <input
          id="amount"
          type="number"
          min="0.01"
          step="0.01"
          placeholder="0.00"
          value={amount}
          onChange={e => setAmount(e.target.value)}
          className={errors.amount ? 'input-error' : ''}
          aria-describedby={errors.amount ? 'err-amount' : undefined}
        />
        {errors.amount && <span className="field-error" id="err-amount">{errors.amount}</span>}
      </div>

      <div className="field">
        <label htmlFor="category">Category</label>
        <select
          id="category"
          value={category}
          onChange={e => setCategory(e.target.value)}
          className={errors.category ? 'input-error' : ''}
        >
          <option value="">— select —</option>
          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        {errors.category && <span className="field-error">{errors.category}</span>}
      </div>

      <div className="field">
        <label htmlFor="description">Description</label>
        <input
          id="description"
          type="text"
          placeholder="e.g. Lunch at Swiggy"
          value={description}
          onChange={e => setDescription(e.target.value)}
          className={errors.description ? 'input-error' : ''}
          maxLength={500}
        />
        {errors.description && <span className="field-error">{errors.description}</span>}
      </div>

      <div className="field">
        <label htmlFor="date">Date</label>
        <input
          id="date"
          type="date"
          value={date}
          onChange={e => setDate(e.target.value)}
          className={errors.date ? 'input-error' : ''}
        />
        {errors.date && <span className="field-error">{errors.date}</span>}
      </div>

      <button type="submit" className="btn-primary" disabled={submitting}>
        {submitting ? 'Saving…' : 'Add Expense'}
      </button>
    </form>
  );
}

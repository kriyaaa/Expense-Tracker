import { useState } from 'react';
import ExpenseForm from './components/ExpenseForm';
import ExpenseList from './components/ExpenseList';

export default function App() {
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  function handleExpenseCreated() {
    setRefreshTrigger(t => t + 1);
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Expense Tracker</h1>
        <span className="header-sub">Track where your money goes</span>
      </header>

      <main className="app-body">
        <aside className="form-panel">
          <ExpenseForm onCreated={handleExpenseCreated} />
        </aside>

        <section className="list-panel">
          <ExpenseList refreshTrigger={refreshTrigger} />
        </section>
      </main>
    </div>
  );
}

export interface Expense {
  id: number;
  amount: number;
  category: string;
  description: string;
  date: string;       // ISO date string "YYYY-MM-DD"
  createdAt: string;  // ISO datetime string
}

export interface CreateExpenseRequest {
  amount: number;
  category: string;
  description: string;
  date: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface ValidationErrors {
  [field: string]: string;
}

export const CATEGORIES = [
  'Food & Drink',
  'Transport',
  'Housing',
  'Health',
  'Shopping',
  'Entertainment',
  'Education',
  'Utilities',
  'Other',
] as const;

export type Category = (typeof CATEGORIES)[number];

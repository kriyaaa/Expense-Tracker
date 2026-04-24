import axios, { AxiosError } from 'axios';
import type { ApiResponse, CreateExpenseRequest, Expense } from '../types';

const api = axios.create({
  baseURL: '/expenses',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10_000,
});

/** POST /expenses — create a new expense */
export async function createExpense(request: CreateExpenseRequest): Promise<Expense> {
  const { data } = await api.post<ApiResponse<Expense>>('', request);
  return data.data;
}

/** GET /expenses — optionally filtered by category */
export async function getExpenses(category?: string): Promise<Expense[]> {
  const params: Record<string, string> = { sort: 'date_desc' };
  if (category) params.category = category;
  const { data } = await api.get<ApiResponse<Expense[]>>('', { params });
  return data.data;
}

/** DELETE /expenses/:id */
export async function deleteExpense(id: number): Promise<void> {
  await api.delete(`/${id}`);
}

/** Extract a human-readable error message from any thrown error. */
export function getErrorMessage(err: unknown): string {
  if (err instanceof AxiosError) {
    const serverMsg = err.response?.data?.message;
    if (serverMsg) return serverMsg;
    if (err.code === 'ECONNABORTED') return 'Request timed out. Check your connection and try again.';
    if (!err.response) return 'Cannot reach the server. Please try again later.';
  }
  return 'Something went wrong. Please try again.';
}

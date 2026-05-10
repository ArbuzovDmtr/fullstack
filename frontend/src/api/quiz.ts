import type { AttemptResult, CreateQuizPayload, LeaderboardEntry, Quiz, QuizAttempt } from '../types';


const BASE = '/api';

export async function fetchQuizzes(): Promise<Quiz[]> {
  const res = await fetch(`${BASE}/quizzes`);
  if (!res.ok) throw new Error('Can`t load quizzes');
  return res.json();
}

export async function fetchQuiz(id: string): Promise<Quiz> {
  const res = await fetch(`${BASE}/quizzes/${id}`);
  if (!res.ok) throw new Error('Quiz not found');
  return res.json();
}

export async function submitAttempt(attempt: QuizAttempt): Promise<QuizAttempt> {
  const res = await fetch(`${BASE}/attempts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(attempt),
  });
  if (!res.ok) throw new Error('Error by sending results');
  return res.json();
}

export async function fetchAttemptResult(attemptId: string): Promise<AttemptResult> {
  const res = await fetch(`${BASE}/attempts/${attemptId}/result`);
  if (!res.ok) throw new Error('Can`t load attempt result');
  return res.json();
}

export async function fetchLeaderboard(quizId: string): Promise<LeaderboardEntry[]> {
  const res = await fetch(`${BASE}/leaderboard/${quizId}`);
  if (!res.ok) throw new Error('Can`t load leaderboard');
  return res.json();
}

export async function fetchAdminQuizzes(): Promise<Quiz[]> {
  const res = await fetch(`${BASE}/admin/quizzes`);
  if (!res.ok) throw new Error('Can`t load admin quizzes');
  return res.json();
}

export async function fetchAdminQuiz(id: string): Promise<Quiz> {
  const res = await fetch(`${BASE}/admin/quizzes/${id}`);
  if (!res.ok) throw new Error('Quiz not found');
  return res.json();
}

export async function createQuiz(payload: CreateQuizPayload): Promise<Quiz> {
  const res = await fetch(`${BASE}/admin/quizzes`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    throw new Error('Failed to create quiz');
  }

  return res.json();
}

export async function updateQuiz(id: string, payload: CreateQuizPayload): Promise<Quiz> {
  const res = await fetch(`${BASE}/admin/quizzes/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    throw new Error('Failed to update quiz');
  }

  return res.json();
}

export async function deleteQuiz(id: string): Promise<void> {
  const res = await fetch(`${BASE}/admin/quizzes/${id}`, {
    method: 'DELETE',
  });

  if (!res.ok) {
    throw new Error('Failed to delete quiz');
  }
}

export async function publishQuiz(id: string): Promise<Quiz> {
  const res = await fetch(`${BASE}/admin/quizzes/${id}/publish`, {
    method: 'PATCH',
  });

  if (!res.ok) {
    throw new Error(await res.text() || 'Failed to publish quiz');
  }

  return res.json();
}

export async function unpublishQuiz(id: string): Promise<Quiz> {
  const res = await fetch(`${BASE}/admin/quizzes/${id}/unpublish`, {
    method: 'PATCH',
  });

  if (!res.ok) {
    throw new Error('Failed to unpublish quiz');
  }

  return res.json();
}

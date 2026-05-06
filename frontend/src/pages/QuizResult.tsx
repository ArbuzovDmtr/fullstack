import { useLocation, useNavigate } from 'react-router-dom';
import type { QuizAttempt } from '../types';

interface LocationState {
  attempt: QuizAttempt;
  quizTitle: string;
}

export default function QuizResult() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState | null;

  if (!state?.attempt) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center px-6">
        <div className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm text-center max-w-md w-full">
          <h1 className="text-xl font-semibold text-blue-700 mb-2">No results</h1>
          <p className="text-gray-700 text-sm mb-5">There is no completed quiz attempt.</p>
          <button
            onClick={() => navigate('/')}
            className="px-4 py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition"
          >
            Main
          </button>
        </div>
      </div>
    );
  }

  const { attempt, quizTitle } = state;
  const { score = 0, maxScore = 0 } = attempt;
  const percent = maxScore > 0 ? Math.round((score / maxScore) * 100) : 0;

  const grade = percent >= 90
    ? { label: 'Perfect', color: 'text-blue-700' }
    : percent >= 70
      ? { label: 'Good', color: 'text-blue-700' }
      : percent >= 50
        ? { label: 'Not bad', color: 'text-blue-700' }
        : { label: 'Try one more time', color: 'text-blue-700' };

  let duration: string | null = null;

  if (attempt.startedAt && attempt.finishedAt) {
    const ms = new Date(attempt.finishedAt).getTime() - new Date(attempt.startedAt).getTime();
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;

    duration = minutes > 0 ? `${minutes} min ${remainingSeconds} sec` : `${remainingSeconds} sec`;
  }

  return (
    <div className="min-h-screen bg-gray-100 text-gray-900 flex flex-col items-center justify-center px-6 py-16">
      <div className="w-full max-w-md bg-white border border-blue-100 rounded-xl p-6 shadow-sm text-center">
        <div className="relative inline-flex items-center justify-center mb-8">
          <svg className="w-36 h-36 -rotate-90" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="54" fill="none" stroke="#e5e7eb" strokeWidth="8" />
            <circle
              cx="60"
              cy="60"
              r="54"
              fill="none"
              stroke="#2563eb"
              strokeWidth="8"
              strokeLinecap="round"
              strokeDasharray={`${2 * Math.PI * 54}`}
              strokeDashoffset={`${2 * Math.PI * 54 * (1 - percent / 100)}`}
              style={{ transition: 'stroke-dashoffset 1s ease' }}
            />
          </svg>

          <div className="absolute flex flex-col items-center">
            <span className="text-3xl font-bold tabular-nums text-blue-700">{percent}%</span>
          </div>
        </div>

        <p className={`text-2xl font-bold mb-1 ${grade.color}`}>{grade.label}</p>
        <h1 className="text-gray-700 text-sm mb-8 truncate px-4">{quizTitle}</h1>

        <div className="grid grid-cols-2 gap-3 mb-10">
          <div className="bg-gray-100 border border-blue-100 rounded-lg p-4">
            <p className="text-xs text-gray-500 mb-1">Points</p>
            <p className="text-xl font-bold text-gray-900">
              {score} <span className="text-gray-500 text-sm">/ {maxScore}</span>
            </p>
          </div>

          {duration && (
            <div className="bg-gray-100 border border-blue-100 rounded-lg p-4">
              <p className="text-xs text-gray-500 mb-1">Time</p>
              <p className="text-xl font-bold text-gray-900">{duration}</p>
            </div>
          )}
        </div>

        <div className="flex flex-col gap-3">
          <button
            onClick={() => navigate(`/quiz/${attempt.quizId}`)}
            className="w-full py-3 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition text-sm font-medium"
          >
            Try one more time
          </button>

          <button
            onClick={() => navigate('/')}
            className="w-full py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition text-sm"
          >
            All quizzes
          </button>
        </div>
      </div>
    </div>
  );
}

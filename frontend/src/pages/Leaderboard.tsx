import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchLeaderboard, fetchQuiz } from '../api/quiz';
import type { LeaderboardEntry, Quiz } from '../types';

export default function Leaderboard() {
  const { quizId } = useParams<{ quizId: string }>();
  const navigate = useNavigate();

  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [entries, setEntries] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!quizId) return;

    Promise.all([
      fetchQuiz(quizId),
      fetchLeaderboard(quizId),
    ])
        .then(([loadedQuiz, loadedEntries]) => {
          setQuiz(loadedQuiz);
          setEntries(loadedEntries);
        })
        .catch((e) =>
            setError(e instanceof Error ? e.message : "Can't load leaderboard")
        )
        .finally(() => setLoading(false));
  }, [quizId]);
  if (loading) {
    return (
        <div className="min-h-screen flex items-center justify-center">
          <p>Loading...</p>
        </div>
    );
  }
  if (!quizId) {
    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center px-6">
          <div className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm text-center max-w-md w-full">
            <h1 className="text-xl font-semibold text-blue-700 mb-2">
              Leaderboard unavailable
            </h1>
            <p className="text-gray-700 text-sm mb-5">Quiz id is missing</p>
            <button
                onClick={() => navigate('/')}
                className="px-4 py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition"
            >
              All quizzes
            </button>
          </div>
        </div>
    );
  }

  if (error || !quiz) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center px-6">
        <div className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm text-center max-w-md w-full">
          <h1 className="text-xl font-semibold text-blue-700 mb-2">Leaderboard unavailable</h1>
          <p className="text-gray-700 text-sm mb-5">{error ?? 'Quiz not found'}</p>
          <button
            onClick={() => navigate('/')}
            className="px-4 py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition"
          >
            All quizzes
          </button>
        </div>
      </div>
    );
  }

  const maxScore = Math.max(...entries.map((entry) => entry.score), 0);

  return (
    <div className="min-h-screen bg-gray-100 text-gray-900 px-6 py-10">
      <main className="max-w-4xl mx-auto">
        <section className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm mb-4">
          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
            <div className="min-w-0">
              <p className="text-sm text-gray-500 mb-1">Leaderboard</p>
              <h1 className="text-2xl font-bold text-blue-700 truncate">{quiz.title}</h1>
              <p className="text-sm text-gray-600 mt-2">
                Best score per user, sorted by points and completion time.
              </p>
            </div>

            <div className="flex flex-wrap gap-2">
              <button
                onClick={() => navigate(`/quiz/${quiz.id}`)}
                className="px-4 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition text-sm"
              >
                Start quiz
              </button>
              <button
                onClick={() => navigate('/')}
                className="px-4 py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition text-sm"
              >
                All quizzes
              </button>
            </div>
          </div>
        </section>

        {entries.length === 0 ? (
          <div className="bg-white border border-dashed border-blue-200 rounded-xl p-12 text-center shadow-sm">
            <h2 className="text-lg font-semibold text-blue-700 mb-2">No results yet</h2>
            <p className="text-gray-700 text-sm">Complete the quiz to appear here.</p>
          </div>
        ) : (
          <div className="bg-white border border-blue-100 rounded-xl shadow-sm overflow-hidden">
            <div className="hidden sm:grid grid-cols-[80px_1fr_120px_140px] gap-4 px-6 py-3 bg-blue-50 border-b border-blue-100 text-xs font-semibold uppercase text-blue-700">
              <span>Rank</span>
              <span>User</span>
              <span>Score</span>
              <span>Time</span>
            </div>

            <div className="divide-y divide-blue-100">
              {entries.map((entry, index) => (
                <LeaderboardRow
                  key={entry.id ?? `${entry.userId}-${index}`}
                  entry={entry}
                  rank={index + 1}
                  maxScore={maxScore}
                />
              ))}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

function LeaderboardRow({
  entry,
  rank,
  maxScore,
}: {
  entry: LeaderboardEntry;
  rank: number;
  maxScore: number;
}) {
  const percent = maxScore > 0 ? Math.round((entry.score / maxScore) * 100) : 0;

  return (
    <div className="grid sm:grid-cols-[80px_1fr_120px_140px] gap-3 sm:gap-4 px-6 py-4 items-center">
      <div className="flex items-center gap-3">
        <span className="w-10 h-10 rounded-full bg-blue-50 border border-blue-100 text-blue-700 font-bold flex items-center justify-center tabular-nums">
          {rank}
        </span>
        <span className="sm:hidden text-sm font-semibold text-gray-500">Rank</span>
      </div>

      <div className="min-w-0">
        <p className="font-semibold text-gray-900 truncate">{entry.userId}</p>
        <div className="mt-2 h-2 bg-gray-100 rounded-full overflow-hidden">
          <div className="h-full bg-blue-600" style={{ width: `${percent}%` }} />
        </div>
      </div>

      <div>
        <p className="sm:hidden text-xs text-gray-500 mb-1">Score</p>
        <p className="font-bold text-blue-700 tabular-nums">{entry.score}</p>
      </div>

      <div>
        <p className="sm:hidden text-xs text-gray-500 mb-1">Time</p>
        <p className="text-gray-800 tabular-nums">{formatTime(entry.timeSpentSeconds)}</p>
      </div>
    </div>
  );
}

function formatTime(seconds: number) {
  const safeSeconds = Math.max(0, seconds);
  const minutes = Math.floor(safeSeconds / 60);
  const remainingSeconds = safeSeconds % 60;

  if (minutes === 0) return `${remainingSeconds} sec`;
  return `${minutes} min ${remainingSeconds.toString().padStart(2, '0')} sec`;
}

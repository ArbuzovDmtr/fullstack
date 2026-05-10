import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchCurrentUser, login, logout } from '../api/auth';
import { fetchQuizzes } from '../api/quiz';
import type { Quiz, User } from '../types';

export default function QuizList() {
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const isAdmin = currentUser?.roles?.includes('ADMIN') ?? false;

  useEffect(() => {
    Promise.all([
      fetchQuizzes(),
      fetchCurrentUser().catch(() => null),
    ])
      .then(([loadedQuizzes, loadedUser]) => {
        setQuizzes(loadedQuizzes);
        setCurrentUser(loadedUser);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center px-6">
        <div className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm">
          <div className="flex gap-2">
            {[0, 1, 2].map((i) => (
              <div
                key={i}
                className="w-3 h-3 rounded-full bg-blue-200 animate-bounce"
                style={{ animationDelay: `${i * 0.15}s` }}
              />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center px-6">
        <div className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm text-center max-w-md w-full">
          <h1 className="text-xl font-semibold text-blue-700 mb-2">Loading Error</h1>
          <p className="text-gray-800 text-sm mb-5">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition"
          >
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 text-gray-900">
      <header className="bg-white border-b border-blue-100 px-6 py-5 shadow-sm">
        <div className="max-w-4xl mx-auto flex items-center justify-between gap-4">
          <div className="flex items-baseline gap-3">
            <h1 className="text-2xl font-bold tracking-tight text-blue-700">Quiz</h1>
            <span className="text-gray-500 text-sm">platform</span>
          </div>

          <div className="flex items-center gap-3">
            {currentUser ? (
              <>
                {currentUser.avatarUrl && (
                  <img
                    src={currentUser.avatarUrl}
                    alt=""
                    className="w-9 h-9 rounded-full border border-blue-100"
                  />
                )}
                <span className="hidden sm:inline text-sm text-gray-700 max-w-44 truncate">
                  {currentUser.name || currentUser.email || 'User'}
                </span>
                <button
                  type="button"
                  onClick={logout}
                  className="px-4 py-2 bg-white text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-50 transition"
                >
                  Logout
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={login}
                className="px-4 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition"
              >
                Login
              </button>
            )}
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-6 py-12">
        <section className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm mb-6">
          <h2 className="text-3xl font-bold tracking-tight text-blue-700 mb-2">Available quizzes</h2>
          <p className="text-gray-600 text-sm">
            Published quizzes: {quizzes.length}
          </p>
        </section>
        {isAdmin && (
          <div className="flex flex-wrap gap-3 mb-4">
            <button
              type="button"
              onClick={() => navigate('/admin/quizzes')}
              className="px-5 py-2 bg-white text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-50 transition"
            >
              Admin quizzes
            </button>
          </div>
        )}
        {quizzes.length === 0 ? (
          <div className="bg-white border border-dashed border-blue-200 rounded-xl p-12 text-center shadow-sm">
            <h3 className="text-lg font-semibold text-blue-700 mb-2">No quizzes yet</h3>
            <p className="text-gray-700 text-sm">There are no published quizzes.</p>
          </div>
        ) : (
          <div className="grid gap-4">
            {quizzes.map((quiz) => (
              <div
                key={quiz.id}
                className="group bg-white border border-blue-100 rounded-xl p-6 shadow-sm hover:border-blue-200 hover:shadow-md transition"
              >
                <div className="flex items-start justify-between gap-4">
                  <button
                    type="button"
                    onClick={() => navigate(`/quiz/${quiz.id}`)}
                    className="flex-1 min-w-0 text-left"
                  >
                    <h3 className="font-semibold text-lg text-blue-700 group-hover:text-blue-800 transition-colors truncate">
                      {quiz.title}
                    </h3>

                    {quiz.description && (
                      <p className="text-gray-700 text-sm mt-1 line-clamp-2">
                        {quiz.description}
                      </p>
                    )}

                    <div className="flex flex-wrap gap-3 mt-4">
                      <span className="text-xs text-gray-700 bg-gray-100 border border-blue-100 rounded-full px-3 py-1">
                        {quiz.questions?.length ?? 0} question{quiz.questions?.length === 1 ? '' : 's'}
                      </span>

                      {quiz.timeLimitSeconds && (
                        <span className="text-xs text-gray-700 bg-gray-100 border border-blue-100 rounded-full px-3 py-1">
                          {Math.floor(quiz.timeLimitSeconds / 60)} min
                        </span>
                      )}
                    </div>
                  </button>

                  <div className="flex items-start gap-3">
                    <button
                      type="button"
                      onClick={() => navigate(`/leaderboard/${quiz.id}`)}
                      className="px-3 py-2 text-sm text-blue-700 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 transition"
                    >
                      Leaderboard
                    </button>

                    <button
                      type="button"
                      onClick={() => navigate(`/quiz/${quiz.id}`)}
                      className="text-blue-600 group-hover:text-blue-800 transition-colors text-xl mt-1"
                      aria-label={`Open ${quiz.title}`}
                    >
                      -&gt;
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { deleteQuiz, fetchAdminQuizzes, publishQuiz, unpublishQuiz } from '../api/quiz';
import type { Quiz } from '../types';

type LocationState = {
  savedQuizId?: string;
};

export default function AdminQuizzesPage() {
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyQuizId, setBusyQuizId] = useState<string | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const savedQuizId = (location.state as LocationState | null)?.savedQuizId;

  useEffect(() => {
    fetchAdminQuizzes()
      .then(setQuizzes)
      .catch((e) => setError(e instanceof Error ? e.message : 'Can`t load quizzes'))
      .finally(() => setLoading(false));
  }, []);

  async function updateStatus(quiz: Quiz, nextPublished: boolean) {
    setBusyQuizId(quiz.id);
    setError(null);

    try {
      const updatedQuiz = nextPublished ? await publishQuiz(quiz.id) : await unpublishQuiz(quiz.id);
      setQuizzes((current) => current.map((item) => (item.id === updatedQuiz.id ? updatedQuiz : item)));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to update quiz status');
    } finally {
      setBusyQuizId(null);
    }
  }

  async function handleDelete(quiz: Quiz) {
    const confirmed = window.confirm(`Delete quiz "${quiz.title}"?`);
    if (!confirmed) return;

    setBusyQuizId(quiz.id);
    setError(null);

    try {
      await deleteQuiz(quiz.id);
      setQuizzes((current) => current.filter((item) => item.id !== quiz.id));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to delete quiz');
    } finally {
      setBusyQuizId(null);
    }
  }

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

  return (
    <div className="min-h-screen bg-gray-100 text-gray-900">
      <header className="bg-white border-b border-blue-100 px-6 py-5 shadow-sm">
        <div className="max-w-6xl mx-auto flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-blue-700">Admin quizzes</h1>
            <p className="text-gray-500 text-sm">All drafts and published quizzes</p>
          </div>

          <div className="flex flex-wrap gap-3">
            <Link
              to="/"
              className="px-4 py-2 bg-white text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-50 transition"
            >
              Public list
            </Link>
            <Link
              to="/admin/create"
              className="px-5 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition"
            >
              Create Quiz
            </Link>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-10">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 mb-6 text-sm">
            {error}
          </div>
        )}

        <section className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm mb-6">
          <div className="grid sm:grid-cols-3 gap-4">
            <Summary label="Total" value={quizzes.length} />
            <Summary label="Published" value={quizzes.filter((quiz) => quiz.published).length} />
            <Summary label="Drafts" value={quizzes.filter((quiz) => !quiz.published).length} />
          </div>
        </section>

        {quizzes.length === 0 ? (
          <div className="bg-white border border-dashed border-blue-200 rounded-xl p-12 text-center shadow-sm">
            <h2 className="text-lg font-semibold text-blue-700 mb-2">No quizzes yet</h2>
            <p className="text-gray-700 text-sm">Create a draft to start building the quiz catalog.</p>
          </div>
        ) : (
          <div className="bg-white border border-blue-100 rounded-xl shadow-sm overflow-hidden">
            <div className="hidden lg:grid grid-cols-[1fr_130px_120px_170px_310px] gap-4 px-6 py-3 bg-blue-50 border-b border-blue-100 text-xs font-semibold uppercase text-blue-700">
              <span>Quiz</span>
              <span>Status</span>
              <span>Questions</span>
              <span>Created</span>
              <span>Actions</span>
            </div>

            <div className="divide-y divide-blue-100">
              {quizzes.map((quiz) => (
                <article
                  key={quiz.id}
                  className={`grid lg:grid-cols-[1fr_130px_120px_170px_310px] gap-4 px-6 py-5 items-center ${
                    savedQuizId === quiz.id ? 'bg-blue-50' : 'bg-white'
                  }`}
                >
                  <div className="min-w-0">
                    <h2 className="font-semibold text-blue-700 truncate">{quiz.title || 'Untitled quiz'}</h2>
                    {quiz.description && (
                      <p className="text-sm text-gray-600 mt-1 line-clamp-2">{quiz.description}</p>
                    )}
                  </div>

                  <StatusBadge published={quiz.published} />

                  <div>
                    <p className="lg:hidden text-xs text-gray-500 mb-1">Questions</p>
                    <p className="text-sm text-gray-800 tabular-nums">{quiz.questions?.length ?? 0}</p>
                  </div>

                  <div>
                    <p className="lg:hidden text-xs text-gray-500 mb-1">Created</p>
                    <p className="text-sm text-gray-800">{formatDate(quiz.createdAt)}</p>
                  </div>

                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => navigate(`/admin/quizzes/${quiz.id}/edit`)}
                      className="px-3 py-2 text-sm text-blue-700 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 transition"
                    >
                      Edit
                    </button>

                    {quiz.published ? (
                      <button
                        type="button"
                        onClick={() => updateStatus(quiz, false)}
                        disabled={busyQuizId === quiz.id}
                        className="px-3 py-2 text-sm text-gray-700 bg-gray-50 border border-gray-200 rounded-lg hover:bg-gray-100 disabled:opacity-60 transition"
                      >
                        Unpublish
                      </button>
                    ) : (
                      <button
                        type="button"
                        onClick={() => updateStatus(quiz, true)}
                        disabled={busyQuizId === quiz.id}
                        className="px-3 py-2 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg hover:bg-green-100 disabled:opacity-60 transition"
                      >
                        Publish
                      </button>
                    )}

                    <button
                      type="button"
                      onClick={() => handleDelete(quiz)}
                      disabled={busyQuizId === quiz.id}
                      className="px-3 py-2 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg hover:bg-red-100 disabled:opacity-60 transition"
                    >
                      {busyQuizId === quiz.id ? 'Working...' : 'Delete'}
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

function Summary({ label, value }: { label: string; value: number }) {
  return (
    <div className="bg-gray-100 border border-blue-100 rounded-lg p-4">
      <p className="text-xs text-gray-500 mb-1">{label}</p>
      <p className="text-2xl font-bold text-blue-700 tabular-nums">{value}</p>
    </div>
  );
}

function StatusBadge({ published }: { published: boolean }) {
  return (
    <span
      className={`w-fit text-xs font-semibold rounded-full px-3 py-1 border ${
        published
          ? 'bg-green-50 text-green-700 border-green-200'
          : 'bg-yellow-50 text-yellow-700 border-yellow-200'
      }`}
    >
      {published ? 'published' : 'draft'}
    </span>
  );
}
const locale = navigator.language.startsWith('de') ? 'de-DE' : 'en-US';
function formatDate(value?: string) {
  if (!value) return 'Unknown';
  return new Intl.DateTimeFormat(locale, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
}

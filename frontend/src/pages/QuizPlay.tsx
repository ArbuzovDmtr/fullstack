import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchCurrentUser } from '../api/auth';
import { fetchQuiz, submitAttempt } from '../api/quiz';
import type { Quiz, User, UserAnswer } from '../types';

function getGuestUserId(): string {
  const key = 'guestUserId';
  let id = localStorage.getItem(key);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(key, id);
  }
  return id;
}

export default function QuizPlay() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<UserAnswer[]>([]);
  const [timeLeft, setTimeLeft] = useState<number | null>(null);

  const startedAt = useRef(new Date().toISOString());
  const questionStartedAt = useRef(Date.now());
  const answersRef = useRef<UserAnswer[]>([]);

  useEffect(() => {
    if (!id) return;

    Promise.all([
      fetchQuiz(id),
      fetchCurrentUser().catch(() => null),
    ])
      .then(([q, user]) => {
        const sorted = {
          ...q,
          questions: [...q.questions].sort((a, b) => a.orderIndex - b.orderIndex),
        };

        setQuiz(sorted);
        setCurrentUser(user);

        if (sorted.timeLimitSeconds) {
          setTimeLeft(sorted.timeLimitSeconds);
        }
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (timeLeft === null || timeLeft <= 0) return;

    const interval = setInterval(() => {
      setTimeLeft((currentTimeLeft) => {
        if (currentTimeLeft === null || currentTimeLeft <= 1) {
          clearInterval(interval);
          return 0;
        }

        return currentTimeLeft - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [timeLeft]);

  useEffect(() => {
    if (timeLeft === 0) {
      void handleSubmit();
    }
  }, [timeLeft]);

  useEffect(() => {
    answersRef.current = answers;
  }, [answers]);

  const currentQuestion = quiz?.questions[currentIndex];
  const currentAnswer = answers.find((answer) => answer.questionId === currentQuestion?.id);
  const isLast = quiz ? currentIndex === quiz.questions.length - 1 : false;
  const hasAnswer = !!(currentAnswer?.selectedOptionIds?.length || currentAnswer?.textAnswer?.trim());
  const progress = quiz ? ((currentIndex + 1) / quiz.questions.length) * 100 : 0;

  function setAnswer(answer: UserAnswer) {
    setAnswers((previousAnswers) => {
      const existingAnswer = previousAnswers.find((previousAnswer) => previousAnswer.questionId === answer.questionId);
      return [
        ...previousAnswers.filter((previousAnswer) => previousAnswer.questionId !== answer.questionId),
        {
          ...existingAnswer,
          ...answer,
          timeSpentSeconds: existingAnswer?.timeSpentSeconds,
        },
      ];
    });
  }

  function withCurrentQuestionTime(sourceAnswers: UserAnswer[]) {
    if (!currentQuestion) return sourceAnswers;

    const elapsedSeconds = Math.max(0, Math.round((Date.now() - questionStartedAt.current) / 1000));
    const existingAnswer = sourceAnswers.find((answer) => answer.questionId === currentQuestion.id);
    const updatedAnswer: UserAnswer = {
      ...existingAnswer,
      questionId: currentQuestion.id,
      timeSpentSeconds: (existingAnswer?.timeSpentSeconds ?? 0) + elapsedSeconds,
    };

    questionStartedAt.current = Date.now();

    return [
      ...sourceAnswers.filter((answer) => answer.questionId !== currentQuestion.id),
      updatedAnswer,
    ];
  }

  function goToQuestion(nextIndex: number) {
    const updatedAnswers = withCurrentQuestionTime(answersRef.current);
    answersRef.current = updatedAnswers;
    setAnswers(updatedAnswers);
    setCurrentIndex(nextIndex);
  }

  async function handleSubmit() {
    if (!quiz || submitting) return;

    setSubmitting(true);
    const finalAnswers = withCurrentQuestionTime(answersRef.current);
    answersRef.current = finalAnswers;
    setAnswers(finalAnswers);

    try {
      const result = await submitAttempt({
        quizId: quiz.id,
        userId: currentUser?.id ?? getGuestUserId(),
        answers: finalAnswers,
        startedAt: startedAt.current,
      });

      navigate(`/result/${result.id}`, { state: { attempt: result, quizTitle: quiz.title } });
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error');
      setSubmitting(false);
    }
  }

  function formatTime(seconds: number) {
    return `${Math.floor(seconds / 60)}:${(seconds % 60).toString().padStart(2, '0')}`;
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

  if (error || !quiz) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center px-6">
        <div className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm text-center max-w-md w-full">
          <h1 className="text-xl font-semibold text-blue-700 mb-2">Error</h1>
          <p className="text-gray-800 text-sm mb-5">{error ?? 'Quiz not found'}</p>
          <button
            onClick={() => navigate('/')}
            className="px-4 py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition"
          >
            ← Назад
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 text-gray-900 flex flex-col">
      <header className="bg-white border-b border-blue-100 px-6 py-4 shadow-sm">
        <div className="max-w-2xl mx-auto flex items-center justify-between gap-4">
          <button
            onClick={() => navigate('/')}
            className="text-blue-700 hover:text-blue-900 transition-colors text-sm font-medium"
          >
            ← Exit
          </button>

          <div className="flex items-center gap-4">
            <span className="text-gray-700 text-sm">
              {currentIndex + 1} / {quiz.questions.length}
            </span>

            {timeLeft !== null && (
              <span className={`text-sm font-semibold tabular-nums ${timeLeft < 30 ? 'text-red-500' : 'text-blue-700'}`}>
                {formatTime(timeLeft)}
              </span>
            )}
          </div>
        </div>

        <div className="max-w-2xl mx-auto mt-4">
          <div className="h-2 bg-gray-100 border border-blue-100 rounded-full overflow-hidden">
            <div
              className="h-full bg-blue-600 transition-all duration-500"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>
      </header>

      <main className="flex-1 flex items-center justify-center px-6 py-12">
        <section className="w-full max-w-2xl bg-white border border-blue-100 rounded-xl p-6 shadow-sm">
          <p className="text-xs text-gray-500 uppercase tracking-widest mb-3">
            {currentQuestion?.type === 'SINGLE_CHOICE' ? 'Choose answer' : 'Type answer'}
          </p>

          <h2 className="text-2xl font-bold leading-snug text-blue-700 mb-8">
            {currentQuestion?.text}
          </h2>

          {currentQuestion?.type === 'SINGLE_CHOICE' && (
            <div className="grid gap-3">
              {currentQuestion.answerOptions?.map((option) => {
                const selected = currentAnswer?.selectedOptionIds?.includes(option.id);

                return (
                  <button
                    key={option.id}
                    onClick={() => setAnswer({ questionId: currentQuestion.id, selectedOptionIds: [option.id] })}
                    className={`w-full text-left px-5 py-4 rounded-lg border transition font-medium ${
                      selected
                        ? 'border-blue-300 bg-blue-50 text-blue-800'
                        : 'border-blue-100 bg-white text-gray-900 hover:bg-blue-50 hover:border-blue-200'
                    }`}
                  >
                    {option.text}
                  </button>
                );
              })}
            </div>
          )}

          {currentQuestion?.type === 'TEXT' && (
            <textarea
              className="w-full bg-white border border-blue-200 focus:border-blue-300 focus:ring-2 focus:ring-blue-100 rounded-lg px-5 py-4 text-gray-900 placeholder-gray-400 resize-none outline-none transition"
              rows={4}
              placeholder="Type your answer."
              value={currentAnswer?.textAnswer ?? ''}
              onChange={(e) => setAnswer({ questionId: currentQuestion.id, textAnswer: e.target.value })}
            />
          )}

          <p className="mt-4 text-xs text-gray-500">
            {currentQuestion?.points} point{currentQuestion?.points === 1 ? '' : 's'}
          </p>
        </section>
      </main>

      <footer className="bg-white border-t border-blue-100 px-6 py-5 shadow-sm">
        <div className="max-w-2xl mx-auto flex justify-between gap-3">
          <button
            onClick={() => goToQuestion(Math.max(0, currentIndex - 1))}
            disabled={currentIndex === 0}
            className="px-5 py-2.5 bg-white border border-blue-200 text-blue-700 rounded-lg hover:bg-blue-50 disabled:opacity-40 disabled:cursor-not-allowed transition text-sm"
          >
            ← Back
          </button>

          {isLast ? (
            <button
              onClick={handleSubmit}
              disabled={submitting}
              className="px-8 py-2.5 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition text-sm"
            >
              {submitting ? 'Sending...' : 'End quiz'}
            </button>
          ) : (
            <button
              onClick={() => goToQuestion(currentIndex + 1)}
              className={`px-8 py-2.5 rounded-lg font-semibold transition text-sm ${
                hasAnswer
                  ? 'bg-blue-600 text-white hover:bg-blue-700'
                  : 'bg-blue-50 text-blue-700 border border-blue-200 hover:bg-blue-100'
              }`}
            >
              Next →
            </button>
          )}
        </div>
      </footer>
    </div>
  );
}

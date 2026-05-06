import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { fetchAttemptResult } from '../api/quiz';
import type { AttemptQuestionResult, AttemptResult, QuizAttempt } from '../types';

interface LocationState {
  attempt?: QuizAttempt;
  quizTitle?: string;
}

type ResultTab = 'summary' | 'answers';

export default function QuizResult() {
  const navigate = useNavigate();
  const location = useLocation();
  const { attemptId } = useParams<{ attemptId: string }>();
  const state = location.state as LocationState | null;
  const resolvedAttemptId = attemptId ?? state?.attempt?.id;

  const [result, setResult] = useState<AttemptResult | null>(null);
  const [loading, setLoading] = useState(!!resolvedAttemptId);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<ResultTab>('summary');

  useEffect(() => {
    if (!resolvedAttemptId) return;

    fetchAttemptResult(resolvedAttemptId)
      .then(setResult)
      .catch((e) => setError(e instanceof Error ? e.message : 'Can`t load attempt result'))
      .finally(() => setLoading(false));
  }, [resolvedAttemptId]);

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

  if (error || !result) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center px-6">
        <div className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm text-center max-w-md w-full">
          <h1 className="text-xl font-semibold text-blue-700 mb-2">No results</h1>
          <p className="text-gray-700 text-sm mb-5">{error ?? 'There is no completed quiz attempt.'}</p>
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

  const percent = result.maxScore > 0 ? Math.round((result.score / result.maxScore) * 100) : 0;
  const grade = percent >= 90
    ? 'Perfect'
    : percent >= 70
      ? 'Good'
      : percent >= 50
        ? 'Not bad'
        : 'Try one more time';

  return (
    <div className="min-h-screen bg-gray-100 text-gray-900 px-6 py-10">
      <main className="max-w-4xl mx-auto">
        <div className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm mb-4">
          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 mb-6">
            <div className="min-w-0">
              <p className="text-sm text-gray-500 mb-1">Quiz result</p>
              <h1 className="text-2xl font-bold text-blue-700 truncate">{result.quizTitle}</h1>
            </div>

            <button
              onClick={() => navigate('/')}
              className="px-4 py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition text-sm"
            >
              All quizzes
            </button>
          </div>

          <div className="grid sm:grid-cols-[160px_1fr] gap-6 items-center">
            <div className="relative inline-flex items-center justify-center justify-self-center">
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
                />
              </svg>

              <div className="absolute flex flex-col items-center">
                <span className="text-3xl font-bold tabular-nums text-blue-700">{percent}%</span>
              </div>
            </div>

            <div>
              <p className="text-2xl font-bold text-blue-700 mb-4">{grade}</p>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                <Stat label="Points" value={`${result.score} / ${result.maxScore}`} />
                <Stat label="Total time" value={formatTime(result.totalTimeSeconds)} />
                <Stat
                  label="Correct"
                  value={`${result.questions.filter((question) => question.correct).length} / ${result.questions.length}`}
                />
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white border border-blue-100 rounded-xl shadow-sm overflow-hidden">
          <div className="flex border-b border-blue-100">
            <TabButton active={activeTab === 'summary'} onClick={() => setActiveTab('summary')}>
              Summary
            </TabButton>
            <TabButton active={activeTab === 'answers'} onClick={() => setActiveTab('answers')}>
              Answers
            </TabButton>
          </div>

          {activeTab === 'summary' ? (
            <div className="p-6 grid sm:grid-cols-2 gap-4">
              <Stat label="Started" value={formatDate(result.startedAt)} />
              <Stat label="Finished" value={formatDate(result.finishedAt)} />
              <Stat label="Average per question" value={formatAverageTime(result)} />
              <Stat label="Attempt id" value={result.attemptId} />
            </div>
          ) : (
            <div className="divide-y divide-blue-100">
              {result.questions.map((question, index) => (
                <QuestionResultCard key={question.questionId} question={question} index={index} />
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-gray-100 border border-blue-100 rounded-lg p-4 min-w-0">
      <p className="text-xs text-gray-500 mb-1">{label}</p>
      <p className="text-lg font-bold text-gray-900 truncate">{value}</p>
    </div>
  );
}

function TabButton({
  active,
  onClick,
  children,
}: {
  active: boolean;
  onClick: () => void;
  children: ReactNode;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex-1 px-4 py-3 text-sm font-semibold transition ${
        active ? 'bg-blue-50 text-blue-700' : 'bg-white text-gray-600 hover:bg-gray-50'
      }`}
    >
      {children}
    </button>
  );
}

function QuestionResultCard({ question, index }: { question: AttemptQuestionResult; index: number }) {
  return (
    <section className="p-6">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3 mb-4">
        <div>
          <p className="text-xs text-gray-500 mb-1">
            Question {index + 1} · {question.points} point{question.points === 1 ? '' : 's'}
          </p>
          <h2 className="text-lg font-semibold text-gray-900">{question.questionText}</h2>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <span
            className={`text-xs font-semibold rounded-full px-3 py-1 border ${
              question.correct
                ? 'bg-green-50 text-green-700 border-green-200'
                : 'bg-red-50 text-red-700 border-red-200'
            }`}
          >
            {question.correct ? 'Correct' : 'Wrong'}
          </span>
          <span className="text-xs text-gray-700 bg-gray-100 border border-blue-100 rounded-full px-3 py-1">
            {formatTime(question.timeSpentSeconds)}
          </span>
        </div>
      </div>

      <div className="grid sm:grid-cols-2 gap-4">
        <AnswerBlock title="Your answer">
          {question.type === 'TEXT' ? (
            <p className="text-sm text-gray-900 whitespace-pre-wrap">{question.userTextAnswer || 'No answer'}</p>
          ) : (
            <AnswerList answers={question.userSelectedOptions} />
          )}
        </AnswerBlock>

        <AnswerBlock title="Correct answer">
          {question.type === 'TEXT' ? (
            <AnswerList answers={question.acceptedTextAnswers.map((text) => ({ id: text, text }))} />
          ) : (
            <AnswerList answers={question.correctOptions} />
          )}
        </AnswerBlock>
      </div>
    </section>
  );
}

function AnswerBlock({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <div className="bg-gray-100 border border-blue-100 rounded-lg p-4">
      <p className="text-xs text-gray-500 mb-2">{title}</p>
      {children}
    </div>
  );
}

function AnswerList({ answers }: { answers: { id: string; text: string }[] }) {
  if (answers.length === 0) {
    return <p className="text-sm text-gray-500">No answer</p>;
  }

  return (
    <ul className="grid gap-2">
      {answers.map((answer) => (
        <li key={answer.id} className="text-sm text-gray-900 bg-white border border-blue-100 rounded-md px-3 py-2">
          {answer.text}
        </li>
      ))}
    </ul>
  );
}

function formatAverageTime(result: AttemptResult) {
  if (result.questions.length === 0) return '0 sec';
  return formatTime(Math.round(result.totalTimeSeconds / result.questions.length));
}

function formatDate(value?: string) {
  if (!value) return 'Unknown';
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

function formatTime(seconds: number) {
  const safeSeconds = Math.max(0, seconds);
  const minutes = Math.floor(safeSeconds / 60);
  const remainingSeconds = safeSeconds % 60;

  if (minutes === 0) return `${remainingSeconds} sec`;
  return `${minutes} min ${remainingSeconds.toString().padStart(2, '0')} sec`;
}

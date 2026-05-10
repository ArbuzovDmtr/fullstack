import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createQuiz, fetchAdminQuiz, updateQuiz } from '../api/quiz';
import type { CreateQuizPayload, CreateQuestionPayload, Question, QuestionType } from '../types';

type FormAnswerOption = {
  id?: string;
  text: string;
  correct: boolean;
};

type FormQuestion = {
  id?: string;
  text: string;
  type: QuestionType;
  points: number;
  answerOptions: FormAnswerOption[];
  acceptedTextAnswers: string[];
};

const emptyQuestion = (): FormQuestion => ({
  text: '',
  type: 'SINGLE_CHOICE',
  points: 1,
  answerOptions: [
    { text: '', correct: true },
    { text: '', correct: false },
  ],
  acceptedTextAnswers: [''],
});

export default function CreateQuiz() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [timeLimitSeconds, setTimeLimitSeconds] = useState<number>(300);
  const [questions, setQuestions] = useState<FormQuestion[]>([emptyQuestion()]);
  const [published, setPublished] = useState(false);
  const [loading, setLoading] = useState(isEditing);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    fetchAdminQuiz(id)
      .then((quiz) => {
        setTitle(quiz.title ?? '');
        setDescription(quiz.description ?? '');
        setTimeLimitSeconds(quiz.timeLimitSeconds ?? 0);
        setQuestions(quiz.questions?.length ? quiz.questions.map(toFormQuestion) : [emptyQuestion()]);
        setPublished(quiz.published);
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load quiz'))
      .finally(() => setLoading(false));
  }, [id]);

  function updateQuestion(index: number, updatedQuestion: FormQuestion) {
    setQuestions((currentQuestions) =>
      currentQuestions.map((question, questionIndex) =>
        questionIndex === index ? updatedQuestion : question,
      ),
    );
  }

  function removeQuestion(index: number) {
    setQuestions((currentQuestions) => currentQuestions.filter((_, questionIndex) => questionIndex !== index));
  }

  function buildPayload(published: boolean): CreateQuizPayload {
    const cleanedQuestions: CreateQuestionPayload[] = questions.map((question, index) => {
      if (question.type === 'SINGLE_CHOICE') {
        return {
          id: question.id,
          text: question.text.trim(),
          type: question.type,
          points: question.points,
          orderIndex: index + 1,
          answerOptions: question.answerOptions
            .filter((option) => option.text.trim() !== '')
            .map((option) => ({ id: option.id, text: option.text.trim(), correct: option.correct })),
          acceptedTextAnswers: [],
        };
      }

      return {
        id: question.id,
        text: question.text.trim(),
        type: question.type,
        points: question.points,
        orderIndex: index + 1,
        answerOptions: [],
        acceptedTextAnswers: question.acceptedTextAnswers
          .map((answer) => answer.trim())
          .filter((answer) => answer !== ''),
      };
    });

    return {
      title: title.trim(),
      description: description.trim(),
      timeLimitSeconds,
      questions: cleanedQuestions,
      published,
    };
  }

  function validate(payload: CreateQuizPayload) {
    if (!payload.title) return 'Title is required';
    if (payload.questions.length === 0) return 'At least one question is required';

    for (const question of payload.questions) {
      if (!question.text) return 'Every question needs text';
      if (question.points < 1) return 'Question points must be at least 1';

      if (question.type === 'SINGLE_CHOICE') {
        if (!question.answerOptions || question.answerOptions.length < 2) {
          return 'Single choice question needs at least two answer options';
        }
        if (!question.answerOptions.some((option) => option.correct)) {
          return 'Single choice question needs one correct answer';
        }
      }

      if (question.type === 'TEXT') {
        if (!question.acceptedTextAnswers || question.acceptedTextAnswers.length === 0) {
          return 'Text question needs at least one accepted answer';
        }
      }
    }

    return null;
  }

  async function handleSave(published: boolean) {
    setError(null);

    const payload = buildPayload(published);
    const validationError = validate(payload);

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setSaving(true);
      const savedQuiz = id ? await updateQuiz(id, payload) : await createQuiz(payload);
      navigate('/admin/quizzes', { state: { savedQuizId: savedQuiz.id } });
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown save error');
    } finally {
      setSaving(false);
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
        <div className="max-w-4xl mx-auto flex items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-blue-700">
              {isEditing ? 'Edit quiz' : 'Create quiz'}
            </h1>
            <p className="text-gray-500 text-sm">
              {isEditing ? 'Update quiz content and status' : 'Admin draft and publish page'}
            </p>
          </div>

          <button
            onClick={() => navigate('/admin/quizzes')}
            className="px-4 py-2 bg-white text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-50 transition"
          >
            Back
          </button>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-6 py-10">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 mb-6 text-sm">
            {error}
          </div>
        )}

        <section className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm mb-6">
          <div className="grid gap-5">
            <label className="grid gap-2">
              <span className="text-sm font-medium text-blue-700">Title</span>
              <input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="border border-blue-200 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-100"
                placeholder="Name of quiz"
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium text-blue-700">Description</span>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="border border-blue-200 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-100 resize-none"
                rows={3}
                placeholder="Quiz description"
              />
            </label>

            <label className="grid gap-2 max-w-xs">
              <span className="text-sm font-medium text-blue-700">Time limit, seconds</span>
              <input
                type="number"
                min={0}
                value={timeLimitSeconds}
                onChange={(e) => setTimeLimitSeconds(Number(e.target.value))}
                className="border border-blue-200 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-100"
              />
            </label>
          </div>
        </section>

        <div className="grid gap-5 mb-6">
          {questions.map((question, questionIndex) => (
            <QuestionEditor
              key={questionIndex}
              question={question}
              questionNumber={questionIndex + 1}
              onChange={(updatedQuestion) => updateQuestion(questionIndex, updatedQuestion)}
              onRemove={() => removeQuestion(questionIndex)}
              canRemove={questions.length > 1}
            />
          ))}
        </div>

        <div className="flex flex-wrap gap-3 justify-between items-center bg-white border border-blue-100 rounded-xl p-5 shadow-sm">
          <button
            onClick={() => setQuestions((currentQuestions) => [...currentQuestions, emptyQuestion()])}
            className="px-4 py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-100 transition"
          >
            + Add question
          </button>

          <div className="flex gap-3">
            <button
              disabled={saving}
              onClick={() => handleSave(false)}
              className="px-5 py-2 bg-white text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-50 disabled:opacity-50 transition"
            >
              Save draft
            </button>

            <button
              disabled={saving}
              onClick={() => handleSave(true)}
              className="px-5 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50 transition"
            >
              Publish
            </button>

            {isEditing && published && (
              <button
                disabled={saving}
                onClick={() => handleSave(published)}
                className="px-5 py-2 bg-gray-900 text-white font-semibold rounded-lg hover:bg-gray-800 disabled:opacity-50 transition"
              >
                Save
              </button>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

function toFormQuestion(question: Question): FormQuestion {
  return {
    id: question.id,
    text: question.text ?? '',
    type: question.type,
    points: question.points,
    answerOptions: question.answerOptions?.length
      ? question.answerOptions.map((option) => ({
        id: option.id,
        text: option.text ?? '',
        correct: Boolean(option.correct),
      }))
      : [
        { text: '', correct: true },
        { text: '', correct: false },
      ],
    acceptedTextAnswers: question.acceptedTextAnswers?.length ? question.acceptedTextAnswers : [''],
  };
}

function QuestionEditor({
  question,
  questionNumber,
  onChange,
  onRemove,
  canRemove,
}: {
  question: FormQuestion;
  questionNumber: number;
  onChange: (question: FormQuestion) => void;
  onRemove: () => void;
  canRemove: boolean;
}) {
  function setType(type: QuestionType) {
    onChange({
      ...question,
      type,
      answerOptions: question.answerOptions.length > 0 ? question.answerOptions : [{ text: '', correct: true }],
      acceptedTextAnswers: question.acceptedTextAnswers.length > 0 ? question.acceptedTextAnswers : [''],
    });
  }

  function updateOption(index: number, updatedOption: FormAnswerOption) {
    onChange({
      ...question,
      answerOptions: question.answerOptions.map((option, optionIndex) =>
        optionIndex === index ? updatedOption : option,
      ),
    });
  }

  function updateAcceptedAnswer(index: number, value: string) {
    onChange({
      ...question,
      acceptedTextAnswers: question.acceptedTextAnswers.map((answer, answerIndex) =>
        answerIndex === index ? value : answer,
      ),
    });
  }

  return (
    <section className="bg-white border border-blue-100 rounded-xl p-6 shadow-sm">
      <div className="flex items-start justify-between gap-4 mb-5">
        <h2 className="text-lg font-bold text-blue-700">Question {questionNumber}</h2>

        {canRemove && (
          <button onClick={onRemove} className="text-sm text-red-600 hover:text-red-700">
            Remove
          </button>
        )}
      </div>

      <div className="grid gap-5">
        <label className="grid gap-2">
          <span className="text-sm font-medium text-blue-700">Question text</span>
          <input
            value={question.text}
            onChange={(e) => onChange({ ...question, text: e.target.value })}
            className="border border-blue-200 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-100"
            placeholder="Text of question"
          />
        </label>

        <div className="grid sm:grid-cols-2 gap-4">
          <label className="grid gap-2">
            <span className="text-sm font-medium text-blue-700">Question type</span>
            <select
              value={question.type}
              onChange={(e) => setType(e.target.value as QuestionType)}
              className="border border-blue-200 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-100 bg-white"
            >
              <option value="SINGLE_CHOICE">SINGLE_CHOICE</option>
              <option value="TEXT">TEXT</option>
            </select>
          </label>

          <label className="grid gap-2">
            <span className="text-sm font-medium text-blue-700">Points</span>
            <input
              type="number"
              min={1}
              value={question.points}
              onChange={(e) => onChange({ ...question, points: Number(e.target.value) })}
              className="border border-blue-200 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-100"
            />
          </label>
        </div>

        {question.type === 'SINGLE_CHOICE' && (
          <div className="grid gap-3">
            <div className="flex items-center justify-between gap-4">
              <h3 className="text-sm font-medium text-blue-700">Answer options</h3>
              <button
                onClick={() => onChange({
                  ...question,
                  answerOptions: [...question.answerOptions, { text: '', correct: false }],
                })}
                className="text-sm text-blue-700 hover:text-blue-900"
              >
                + Add option
              </button>
            </div>

            {question.answerOptions.map((option, optionIndex) => (
              <div key={optionIndex} className="flex gap-3 items-center">
                <input
                  value={option.text}
                  onChange={(e) => updateOption(optionIndex, { ...option, text: e.target.value })}
                  className="flex-1 border border-blue-200 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-100"
                  placeholder={`Option ${optionIndex + 1}`}
                />

                <label className="flex items-center gap-2 text-sm text-gray-700 whitespace-nowrap">
                  <input
                    type="radio"
                    checked={option.correct}
                    onChange={() => onChange({
                      ...question,
                      answerOptions: question.answerOptions.map((currentOption, currentIndex) => ({
                        ...currentOption,
                        correct: currentIndex === optionIndex,
                      })),
                    })}
                  />
                  correct
                </label>

                {question.answerOptions.length > 2 && (
                  <button
                    onClick={() => onChange({
                      ...question,
                      answerOptions: question.answerOptions.filter((_, currentIndex) => currentIndex !== optionIndex),
                    })}
                    className="text-sm text-red-600 hover:text-red-700"
                  >
                    x
                  </button>
                )}
              </div>
            ))}
          </div>
        )}

        {question.type === 'TEXT' && (
          <div className="grid gap-3">
            <div className="flex items-center justify-between gap-4">
              <h3 className="text-sm font-medium text-blue-700">Accepted text answers</h3>
              <button
                onClick={() => onChange({
                  ...question,
                  acceptedTextAnswers: [...question.acceptedTextAnswers, ''],
                })}
                className="text-sm text-blue-700 hover:text-blue-900"
              >
                + Add accepted answer
              </button>
            </div>

            {question.acceptedTextAnswers.map((answer, answerIndex) => (
              <div key={answerIndex} className="flex gap-3 items-center">
                <input
                  value={answer}
                  onChange={(e) => updateAcceptedAnswer(answerIndex, e.target.value)}
                  className="flex-1 border border-blue-200 rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-100"
                  placeholder={`Accepted answer ${answerIndex + 1}`}
                />

                {question.acceptedTextAnswers.length > 1 && (
                  <button
                    onClick={() => onChange({
                      ...question,
                      acceptedTextAnswers: question.acceptedTextAnswers.filter((_, currentIndex) => currentIndex !== answerIndex),
                    })}
                    className="text-sm text-red-600 hover:text-red-700"
                  >
                    x
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

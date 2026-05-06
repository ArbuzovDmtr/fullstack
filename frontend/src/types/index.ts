export type QuestionType = 'SINGLE_CHOICE' | 'TEXT';

export interface AnswerOption {
  id: string;
  text: string;
  correct?: boolean;
}

export interface Question {
  id: string;
  text: string;
  type: QuestionType;
  answerOptions?: AnswerOption[];
  acceptedTextAnswers?: string[];
  points: number;
  orderIndex: number;
}

export interface Quiz {
  id: string;
  title: string;
  description: string;
  createdByUserId: string;
  timeLimitSeconds?: number;
  questions: Question[];
  published: boolean;
  createdAt: string;
}

export interface UserAnswer {
  questionId: string;
  selectedOptionIds?: string[];
  textAnswer?: string;
}

export interface QuizAttempt {
  id?: string;
  quizId: string;
  userId: string;
  answers: UserAnswer[];
  score?: number;
  maxScore?: number;
  startedAt?: string;
  finishedAt?: string;
}
export interface CreateAnswerOptionPayload {
  text: string;
  correct?: boolean;
}

export interface CreateQuestionPayload {
  text: string;
  type: QuestionType;
  answerOptions?: CreateAnswerOptionPayload[];
  acceptedTextAnswers?: string[];
  points: number;
  orderIndex: number;
}

export interface CreateQuizPayload {
  title: string;
  description: string;
  timeLimitSeconds?: number;
  questions: CreateQuestionPayload[];
  published: boolean;
}
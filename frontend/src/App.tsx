import { BrowserRouter, Routes, Route } from 'react-router-dom';
import QuizList from './pages/QuizList';
import QuizPlay from './pages/QuizPlay';
import QuizResult from './pages/QuizResult';
import CreateQuiz from './pages/CreateQuiz';
import Leaderboard from './pages/Leaderboard';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<QuizList />} />
          <Route path="/admin/create" element={<CreateQuiz />} />
        <Route path="/quiz/:id" element={<QuizPlay />} />
        <Route path="/result" element={<QuizResult />} />
        <Route path="/result/:attemptId" element={<QuizResult />} />
        <Route path="/leaderboard/:quizId" element={<Leaderboard />} />

      </Routes>
    </BrowserRouter>
  );
}

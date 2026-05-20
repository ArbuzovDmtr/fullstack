import { BrowserRouter, Routes, Route } from 'react-router-dom';
import QuizList from './pages/QuizList';
import QuizPlay from './pages/QuizPlay';
import QuizResult from './pages/QuizResult';
import CreateQuiz from './pages/CreateQuiz';
import Leaderboard from './pages/Leaderboard';
import AdminQuizzesPage from './pages/AdminQuizzesPage';
import AdminRoute from './components/AdminRoute';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<QuizList />} />
        <Route path="/admin/quizzes" element={<AdminRoute><AdminQuizzesPage /></AdminRoute>} />
        <Route path="/admin/create" element={<AdminRoute><CreateQuiz /></AdminRoute>} />
        <Route path="/admin/quizzes/:id/edit" element={<AdminRoute><CreateQuiz /></AdminRoute>} />
        <Route path="/quiz/:id" element={<QuizPlay />} />
        <Route path="/result" element={<QuizResult />} />
        <Route path="/result/:attemptId" element={<QuizResult />} />
        <Route path="/leaderboard/:quizId" element={<Leaderboard />} />
      </Routes>
    </BrowserRouter>
  );
}

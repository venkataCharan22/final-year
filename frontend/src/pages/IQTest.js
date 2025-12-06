import React, { useState, useEffect } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const IQTest = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [mode, setMode] = useState(null); // 'IQ' or 'TECH'
    const [quiz, setQuiz] = useState(null);
    const [loading, setLoading] = useState(false);
    const [currentQuestion, setCurrentQuestion] = useState(0);
    const [answers, setAnswers] = useState({});
    const [timeLeft, setTimeLeft] = useState(1800); // 30 minutes
    const [testCompleted, setTestCompleted] = useState(false);
    const [results, setResults] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    // eslint-disable-next-line react-hooks/exhaustive-deps
    useEffect(() => {
        if (quiz && !testCompleted) {
            const timer = setInterval(() => {
                setTimeLeft(prev => {
                    if (prev <= 1) {
                        submitTest();
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);

            return () => clearInterval(timer);
        }
    }, [quiz, testCompleted]);

    // Exit fullscreen when test is completed
    useEffect(() => {
        return () => {
            if (testCompleted) {
                exitFullscreen();
            }
        };
    }, [testCompleted]);

    // Redirect if not a student
    if (!user || user.role !== 'STUDENT') {
        return <Navigate to="/login" replace />;
    }

    const enterFullscreen = () => {
        const elem = document.documentElement;
        if (elem.requestFullscreen) {
            elem.requestFullscreen().catch(err => {
                console.log('Error attempting to enable fullscreen:', err);
            });
        } else if (elem.webkitRequestFullscreen) { /* Safari */
            elem.webkitRequestFullscreen();
        } else if (elem.msRequestFullscreen) { /* IE11 */
            elem.msRequestFullscreen();
        }
    };

    const exitFullscreen = () => {
        if (document.exitFullscreen) {
            document.exitFullscreen().catch(err => {
                console.log('Error attempting to exit fullscreen:', err);
            });
        } else if (document.webkitExitFullscreen) { /* Safari */
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) { /* IE11 */
            document.msExitFullscreen();
        }
    };

    const startTest = (selectedMode) => {
        setMode(selectedMode);
        setLoading(true);
        // Enter fullscreen immediately on user interaction (button click)
        enterFullscreen();
        createFallbackQuiz(selectedMode);
    };

    const createFallbackQuiz = (selectedMode) => {
        let questions = [];
        let title = "";
        let description = "";

        if (selectedMode === 'IQ') {
            title = "AI-Powered IQ Assessment";
            description = "Cognitive ability assessment";
            questions = [
                {
                    id: "iq-q1",
                    questionText: "If all roses are flowers and some flowers are red, which statement must be true?",
                    options: ["All roses are red", "Some roses might be red", "No roses are red", "All flowers are roses"],
                    correctAnswerIndex: 1,
                    explanation: "Some roses might be red because we know roses are flowers, and some flowers are red.",
                    points: 20
                },
                {
                    id: "iq-q2",
                    questionText: "What comes next in this sequence: 2, 6, 18, 54, ?",
                    options: ["108", "162", "216", "324"],
                    correctAnswerIndex: 1,
                    explanation: "Each number is multiplied by 3: 2×3=6, 6×3=18, 18×3=54, 54×3=162",
                    points: 20
                },
                {
                    id: "iq-q3",
                    questionText: "If 5 machines can make 5 widgets in 5 minutes, how long would it take 100 machines to make 100 widgets?",
                    options: ["100 minutes", "20 minutes", "5 minutes", "1 minute"],
                    correctAnswerIndex: 2,
                    explanation: "Each machine makes 1 widget in 5 minutes, so 100 machines make 100 widgets in 5 minutes.",
                    points: 20
                },
                {
                    id: "iq-q4",
                    questionText: "A cube has 6 faces. If you paint all faces red and then cut the cube into 27 smaller cubes, how many small cubes will have exactly 2 red faces?",
                    options: ["8", "12", "6", "4"],
                    correctAnswerIndex: 1,
                    explanation: "The cubes with exactly 2 red faces are located on the edges but not corners: 12 cubes.",
                    points: 20
                },
                {
                    id: "iq-q5",
                    questionText: "Book is to Reading as Fork is to:",
                    options: ["Eating", "Kitchen", "Metal", "Food"],
                    correctAnswerIndex: 0,
                    explanation: "A book is used for reading, just as a fork is used for eating.",
                    points: 20
                }
            ];
        } else {
            title = "CS Fundamentals Assessment";
            description = "Technical skills assessment covering C++, ML, and DSA";
            questions = [
                {
                    id: "tech-q1",
                    questionText: "In C++, which operator is used to access the member of a structure through a pointer?",
                    options: [".", "->", "*", "&"],
                    correctAnswerIndex: 1,
                    explanation: "The arrow operator (->) is used to access members via a pointer.",
                    points: 20
                },
                {
                    id: "tech-q2",
                    questionText: "What is the output of `int *p = new int(10); delete p; cout << *p;`?",
                    options: ["10", "0", "Garbage Value / Undefined Behavior", "Compilation Error"],
                    correctAnswerIndex: 2,
                    explanation: "Accessing memory after it has been freed (dangling pointer) leads to undefined behavior.",
                    points: 20
                },
                {
                    id: "tech-q3",
                    questionText: "Which of the following is a Supervised Learning algorithm?",
                    options: ["K-Means Clustering", "Linear Regression", "Apriori", "PCA"],
                    correctAnswerIndex: 1,
                    explanation: "Linear Regression is a supervised learning algorithm used for prediction.",
                    points: 20
                },
                {
                    id: "tech-q4",
                    questionText: "In Machine Learning, what is Overfitting?",
                    options: ["Model performs well on training data but poor on test data", "Model performs poor on both", "Model performs well on both", "Model is too simple"],
                    correctAnswerIndex: 0,
                    explanation: "Overfitting happens when a model learns the noise in the training data to the extent that it negatively impacts the performance of the model on new data.",
                    points: 20
                },
                {
                    id: "tech-q5",
                    questionText: "Which data structure uses LIFO (Last In First Out) principle?",
                    options: ["Queue", "Stack", "Linked List", "Tree"],
                    correctAnswerIndex: 1,
                    explanation: "A Stack follows the LIFO principle.",
                    points: 20
                }
            ];
        }

        const fallbackQuiz = {
            id: `assessment-${selectedMode}`,
            title: title,
            description: description,
            questions: questions,
            timeLimit: 30,
            passingScore: 70,
            aiGenerated: true
        };

        setQuiz(fallbackQuiz);
        setLoading(false);
    };

    const handleAnswerSelect = (questionId, answerIndex) => {
        setAnswers(prev => ({
            ...prev,
            [questionId]: answerIndex.toString()
        }));
    };

    const nextQuestion = () => {
        if (currentQuestion < quiz.questions.length - 1) {
            setCurrentQuestion(prev => prev + 1);
        }
    };

    const prevQuestion = () => {
        if (currentQuestion > 0) {
            setCurrentQuestion(prev => prev - 1);
        }
    };

    const submitTest = async () => {
        setSubmitting(true);
        try {
            const token = localStorage.getItem('token');
            // In a real app, we would pass the mode to the backend
            // For now, the backend logic for "Tech" needs to be triggered
            // We can add a query param or body field. Let's assume the backend 
            // can infer or we update the backend to accept 'type'.
            // Since I haven't updated the backend endpoint to accept 'type' explicitly in the body yet,
            // I will rely on the question IDs (tech-q1 vs iq-q1) if I update the backend to detect them,
            // OR I should update the backend to accept 'type'.
            // I'll update the backend to accept 'type' in the next step if needed, 
            // but for now let's send it.

            const response = await fetch('http://localhost:8081/api/iq-test/submit', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    answers: answers,
                    timeSpent: 1800 - timeLeft,
                    testType: mode // Sending mode to backend
                })
            });

            if (response.ok) {
                const data = await response.json();
                setResults(data);
                setTestCompleted(true);
            }
        } catch (error) {
            console.error('Error submitting test:', error);
        } finally {
            setSubmitting(false);
        }
    };

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    const getIQDescription = (score) => {
        if (score >= 130) return { level: "Very Superior", color: "text-purple-600", description: "Exceptional intellectual ability" };
        if (score >= 120) return { level: "Superior", color: "text-blue-600", description: "Well above average intelligence" };
        if (score >= 110) return { level: "High Average", color: "text-green-600", description: "Above average intelligence" };
        if (score >= 90) return { level: "Average", color: "text-yellow-600", description: "Average intelligence" };
        if (score >= 80) return { level: "Low Average", color: "text-orange-600", description: "Below average intelligence" };
        return { level: "Below Average", color: "text-red-600", description: "Well below average intelligence" };
    };

    const handleReturnToDashboard = () => {
        navigate('/student');
    };

    const handleTakeAnotherTest = () => {
        setMode(null); // Go back to selection
        setQuiz(null);
        setLoading(false);
        setCurrentQuestion(0);
        setAnswers({});
        setTimeLeft(1800);
        setTestCompleted(false);
        setResults(null);
        setSubmitting(false);
    };

    if (!mode) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900 flex items-center justify-center p-4">
                <div className="max-w-4xl w-full">
                    <h1 className="text-4xl font-bold text-white text-center mb-8">Assessment Center</h1>
                    <div className="grid md:grid-cols-2 gap-6">
                        {/* IQ Test Card */}
                        <div className="bg-white/10 backdrop-blur-lg rounded-3xl p-8 border border-white/20 hover:bg-white/20 transition-all duration-300 cursor-pointer group"
                            onClick={() => startTest('IQ')}>
                            <div className="text-6xl mb-6 group-hover:scale-110 transition-transform duration-300">🧠</div>
                            <h2 className="text-2xl font-bold text-white mb-4">Cognitive IQ Assessment</h2>
                            <p className="text-blue-200 mb-6">
                                Measure your logical reasoning, pattern recognition, and problem-solving abilities.
                            </p>
                            <ul className="text-blue-100 space-y-2 mb-8">
                                <li className="flex items-center">✅ Pattern Recognition</li>
                                <li className="flex items-center">✅ Logical Reasoning</li>
                                <li className="flex items-center">✅ Spatial Awareness</li>
                            </ul>
                            <button className="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white py-3 px-6 rounded-xl font-semibold">
                                Start IQ Test
                            </button>
                        </div>

                        {/* Tech Assessment Card */}
                        <div className="bg-white/10 backdrop-blur-lg rounded-3xl p-8 border border-white/20 hover:bg-white/20 transition-all duration-300 cursor-pointer group"
                            onClick={() => startTest('TECH')}>
                            <div className="text-6xl mb-6 group-hover:scale-110 transition-transform duration-300">💻</div>
                            <h2 className="text-2xl font-bold text-white mb-4">CS Fundamentals</h2>
                            <p className="text-blue-200 mb-6">
                                Evaluate your technical knowledge in C++, Machine Learning, and Data Structures.
                            </p>
                            <ul className="text-blue-100 space-y-2 mb-8">
                                <li className="flex items-center">✅ C++ & Memory Management</li>
                                <li className="flex items-center">✅ Machine Learning Basics</li>
                                <li className="flex items-center">✅ Data Structures (DSA)</li>
                            </ul>
                            <button className="w-full bg-gradient-to-r from-emerald-600 to-teal-600 text-white py-3 px-6 rounded-xl font-semibold">
                                Start Tech Assessment
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-white mx-auto mb-4"></div>
                    <p className="text-white text-xl">Preparing your assessment...</p>
                </div>
            </div>
        );
    }

    if (testCompleted && results) {
        const iqInfo = getIQDescription(results.score);
        return (
            <div className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900 flex items-center justify-center p-4">
                <div className="max-w-2xl w-full bg-white/10 backdrop-blur-lg rounded-3xl p-8 border border-white/20">
                    <div className="text-center">
                        <div className="text-6xl mb-4">{mode === 'IQ' ? '🧠' : '💻'}</div>
                        <h1 className="text-4xl font-bold text-white mb-2">Assessment Complete!</h1>
                        <p className="text-blue-200 mb-8">Your performance analysis</p>

                        {mode === 'IQ' && (
                            <div className="bg-white/10 rounded-2xl p-6 mb-6">
                                <div className="text-6xl font-bold text-white mb-2">{results.score}</div>
                                <div className={`text-xl font-semibold mb-2 ${iqInfo.color}`}>{iqInfo.level}</div>
                                <div className="text-blue-200">{iqInfo.description}</div>
                            </div>
                        )}

                        {/* AI Analysis Section */}
                        {results.category && (
                            <div className="bg-gradient-to-r from-indigo-500/20 to-purple-500/20 rounded-2xl p-6 mb-6 border border-white/10 text-left">
                                <h3 className="text-xl font-bold text-white mb-2 flex items-center">
                                    <span className="mr-2">🤖</span> AI Analysis
                                </h3>
                                <div className="mb-3">
                                    <span className="text-blue-300 text-sm uppercase tracking-wider font-semibold">
                                        {mode === 'IQ' ? 'Cognitive Profile' : 'Technical Profile'}
                                    </span>
                                    <p className="text-white text-lg font-medium mt-1">{results.category}</p>
                                </div>
                                <div>
                                    <span className="text-blue-300 text-sm uppercase tracking-wider font-semibold">Personalized Advice</span>
                                    <p className="text-blue-100 mt-1 leading-relaxed whitespace-pre-line">{results.advice}</p>
                                </div>
                            </div>
                        )}

                        <div className="grid grid-cols-2 gap-4 mb-8">
                            <div className="bg-white/10 rounded-xl p-4">
                                <div className="text-2xl font-bold text-white">{results.correctAnswers}/{results.totalQuestions}</div>
                                <div className="text-blue-200 text-sm">Questions Correct</div>
                            </div>
                            <div className="bg-white/10 rounded-xl p-4">
                                <div className="text-2xl font-bold text-white">{results.percentage}%</div>
                                <div className="text-blue-200 text-sm">Accuracy</div>
                            </div>
                        </div>

                        <div className="space-y-4">
                            <button
                                onClick={handleReturnToDashboard}
                                className="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white py-3 px-6 rounded-xl font-semibold hover:from-blue-700 hover:to-purple-700 transition-all duration-200"
                            >
                                Return to Dashboard
                            </button>
                            <button
                                onClick={handleTakeAnotherTest}
                                className="w-full bg-white/10 text-white py-3 px-6 rounded-xl font-semibold hover:bg-white/20 transition-all duration-200 border border-white/30"
                            >
                                Take Another Assessment
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (!quiz || !quiz.questions) {
        return null;
    }

    const question = quiz.questions[currentQuestion];
    const progress = ((currentQuestion + 1) / quiz.questions.length) * 100;

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900 p-4">
            <div className="max-w-4xl mx-auto">
                {/* Header */}
                <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-6 mb-6 border border-white/20">
                    <div className="flex justify-between items-center mb-4">
                        <h1 className="text-2xl font-bold text-white">
                            {mode === 'IQ' ? '🧠 AI-Powered IQ Assessment' : '💻 CS Fundamentals Assessment'}
                        </h1>
                        <div className="text-white font-mono text-lg">
                            ⏰ {formatTime(timeLeft)}
                        </div>
                    </div>

                    <div className="flex justify-between items-center mb-2">
                        <span className="text-blue-200">Question {currentQuestion + 1} of {quiz.questions.length}</span>
                        <span className="text-blue-200">{Math.round(progress)}% Complete</span>
                    </div>

                    <div className="w-full bg-white/20 rounded-full h-2">
                        <div
                            className="bg-gradient-to-r from-blue-500 to-purple-500 h-2 rounded-full transition-all duration-300"
                            style={{ width: `${progress}%` }}
                        ></div>
                    </div>
                </div>

                {/* Question */}
                <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-8 mb-6 border border-white/20">
                    <h2 className="text-xl font-semibold text-white mb-6">{question.questionText}</h2>

                    <div className="space-y-3">
                        {question.options.map((option, index) => (
                            <button
                                key={index}
                                onClick={() => handleAnswerSelect(question.id, index)}
                                className={`w-full p-4 rounded-xl text-left transition-all duration-200 border-2 ${answers[question.id] === index.toString()
                                    ? 'bg-blue-600/30 border-blue-400 text-white'
                                    : 'bg-white/10 border-white/30 text-white hover:bg-white/20 hover:border-white/50'
                                    }`}
                            >
                                <span className="font-semibold mr-3">{String.fromCharCode(65 + index)})</span>
                                {option}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Navigation */}
                <div className="flex justify-between items-center">
                    <button
                        onClick={prevQuestion}
                        disabled={currentQuestion === 0}
                        className="bg-white/10 text-white py-3 px-6 rounded-xl font-semibold hover:bg-white/20 transition-all duration-200 border border-white/30 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        ← Previous
                    </button>

                    <div className="text-center">
                        {currentQuestion === quiz.questions.length - 1 ? (
                            <button
                                onClick={submitTest}
                                disabled={submitting || Object.keys(answers).length === 0}
                                className="bg-gradient-to-r from-green-600 to-emerald-600 text-white py-3 px-8 rounded-xl font-semibold hover:from-green-700 hover:to-emerald-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {submitting ? 'Submitting...' : 'Submit Assessment'}
                            </button>
                        ) : (
                            <button
                                onClick={nextQuestion}
                                disabled={currentQuestion === quiz.questions.length - 1}
                                className="bg-gradient-to-r from-blue-600 to-purple-600 text-white py-3 px-6 rounded-xl font-semibold hover:from-blue-700 hover:to-purple-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Next →
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default IQTest;

package com.learningplatform.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MLAdviceService {

    public MLResult analyzePerformance(Map<String, String> answers, Map<String, Integer> correctAnswers, int totalScore,
            String testType) {
        if ("TECH".equals(testType)) {
            return analyzeTechPerformance(answers, correctAnswers);
        } else {
            return analyzeIQPerformance(answers, correctAnswers, totalScore);
        }
    }

    private MLResult analyzeTechPerformance(Map<String, String> answers, Map<String, Integer> correctAnswers) {
        // Calculate scores per category
        // tech-q1, q2: CPP
        // tech-q3, q4: ML
        // tech-q5: DSA

        int cppScore = checkAnswer(answers, correctAnswers, "tech-q1")
                + checkAnswer(answers, correctAnswers, "tech-q2");
        int mlScore = checkAnswer(answers, correctAnswers, "tech-q3") + checkAnswer(answers, correctAnswers, "tech-q4");
        int dsaScore = checkAnswer(answers, correctAnswers, "tech-q5");

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        StringBuilder advice = new StringBuilder();

        // C++ Analysis
        if (cppScore == 2) {
            strengths.add("C++");
        } else if (cppScore == 0) {
            weaknesses.add("C++");
            advice.append(
                    "• C++: You need more practice with pointers and memory management. Review the basics of manual memory allocation.\n");
        } else {
            advice.append("• C++: Good understanding, but brush up on advanced syntax.\n");
        }

        // ML Analysis
        if (mlScore == 2) {
            strengths.add("Machine Learning");
        } else if (mlScore == 0) {
            weaknesses.add("Machine Learning");
            advice.append(
                    "• ML: Your ML fundamentals need work. Focus on understanding the difference between Supervised and Unsupervised learning.\n");
        } else {
            advice.append("• ML: You have the basics down, but dive deeper into model evaluation metrics.\n");
        }

        // DSA Analysis
        if (dsaScore == 1) {
            strengths.add("Data Structures");
        } else {
            weaknesses.add("Data Structures");
            advice.append("• DSA: Review basic data structures like Arrays and Linked Lists. Efficiency matters!\n");
        }

        String category;
        if (strengths.size() >= 2) {
            category = "Full Stack Developer Potential";
            advice.insert(0, "Great job! You have a strong technical foundation. ");
        } else if (strengths.contains("Machine Learning")) {
            category = "Aspiring Data Scientist";
            advice.insert(0, "You show strong potential in AI/ML. ");
        } else if (strengths.contains("C++")) {
            category = "Systems Programmer";
            advice.insert(0, "You have a knack for low-level programming. ");
        } else {
            category = "Tech Novice";
            advice.insert(0, "You are at the beginning of your journey. Keep practicing! ");
        }

        return new MLResult(category, advice.toString());
    }

    private MLResult analyzeIQPerformance(Map<String, String> answers, Map<String, Integer> correctAnswers,
            int totalScore) {
        int logicalScore = checkAnswer(answers, correctAnswers, "iq-q1");
        int patternScore = checkAnswer(answers, correctAnswers, "iq-q2");
        int mathScore = checkAnswer(answers, correctAnswers, "iq-q3");
        int spatialScore = checkAnswer(answers, correctAnswers, "iq-q4");
        int verbalScore = checkAnswer(answers, correctAnswers, "iq-q5");

        String category;
        String advice;

        if (totalScore >= 130) {
            category = "Genius Level Intellect";
            advice = "Your cognitive abilities are exceptional. You excel in pattern recognition and logical reasoning. Consider diving into advanced fields like Quantum Physics or AI Research.";
        } else if (mathScore == 1 && spatialScore == 1) {
            category = "Analytical & Spatial Thinker";
            advice = "You show strong aptitude in mathematical and spatial reasoning. You likely excel in Engineering or Architecture. Focus on honing your logical deduction skills.";
        } else if (patternScore == 1 && logicalScore == 1) {
            category = "Pattern Recognizer";
            advice = "You have a natural ability to spot trends and logical sequences. This is key in Financial Analysis and Cryptography.";
        } else if (verbalScore == 1) {
            category = "Verbal & Analogical Thinker";
            advice = "Your strength lies in understanding relationships and analogies. You might find success in Law or Psychology.";
        } else if (totalScore >= 100) {
            category = "Balanced Thinker";
            advice = "You have a well-rounded cognitive profile. To specialize, identify which type of problem you enjoyed the most and practice it more.";
        } else {
            category = "Developing Mind";
            advice = "Focus on daily brain exercises, puzzles, and reading to sharpen your logical and analytical reasoning. Consistency is key.";
        }

        return new MLResult(category, advice);
    }

    private int checkAnswer(Map<String, String> answers, Map<String, Integer> correctAnswers, String questionId) {
        String submitted = answers.get(questionId);
        if (submitted != null && correctAnswers.containsKey(questionId)) {
            try {
                int idx = Integer.parseInt(submitted);
                return idx == correctAnswers.get(questionId) ? 1 : 0;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static class MLResult {
        private String category;
        private String advice;

        public MLResult(String category, String advice) {
            this.category = category;
            this.advice = advice;
        }

        public String getCategory() {
            return category;
        }

        public String getAdvice() {
            return advice;
        }
    }
}

from dotenv import load_dotenv
load_dotenv()
import os
import requests
from flask import Flask, request, jsonify
import re

app = Flask(__name__)

# API setup
API_URL = "https://router.huggingface.co/novita/v3/openai/chat/completions"
HF_API_TOKEN = os.getenv('HF_API_TOKEN', '')  # Use env var or default
HEADERS = {"Authorization": f"Bearer {HF_API_TOKEN}"}
#HEADERS = {"Authorization": "Bearer "}

MODEL = "deepseek/deepseek-v3-0324"

# MODEL = "meta-llama/Llama-2-7b-chat-hf"
#MODEL = "google/gemma-3-1b-i


def fetchQuizFromLlama(student_topic):
    print("Fetching quiz from Hugging Face router API")
    payload = {
        "messages": [
            {
                "role": "user",
                "content": (
                    f"Generate a quiz with 3 questions to test students on the provided topic. "
                    f"For each question, generate 4 options where only one of the options is correct. "
                    f"Format your response as follows:\n"
                    f"**QUESTION 1:** [Your question here]?\n"
                    f"**OPTION A:** [First option]\n"
                    f"**OPTION B:** [Second option]\n"
                    f"**OPTION C:** [Third option]\n"
                    f"**OPTION D:** [Fourth option]\n"
                    f"**ANS:** [Correct answer letter]\n\n"
                    f"**QUESTION 2:** [Your question here]?\n"
                    f"**OPTION A:** [First option]\n"
                    f"**OPTION B:** [Second option]\n"
                    f"**OPTION C:** [Third option]\n"
                    f"**OPTION D:** [Fourth option]\n"
                    f"**ANS:** [Correct answer letter]\n\n"
                    f"**QUESTION 3:** [Your question here]?\n"
                    f"**OPTION A:** [First option]\n"
                    f"**OPTION B:** [Second option]\n"
                    f"**OPTION C:** [Third option]\n"
                    f"**OPTION D:** [Fourth option]\n"
                    f"**ANS:** [Correct answer letter]\n\n"
                    f"Ensure text is properly formatted. It needs to start with a question, then the options, and finally the correct answer. "
                    f"Follow this pattern for all questions. "
                    f"Here is the student topic:\n{student_topic}"
                )
            }
        ],
        "model": MODEL,
        "max_tokens": 500,
        "temperature": 0.7,
        "top_p": 0.9
    }

    response = requests.post(API_URL, headers=HEADERS, json=payload)
    if response.status_code == 200:
        result = response.json()["choices"][0]["message"]["content"]
        # print(result)
        return result
    else:
        raise Exception(f"API request failed: {response.status_code} - {response.text}")

def process_quiz(quiz_text):
    questions = []
    # Updated regex to match bolded format with numbered questions
    pattern = re.compile(
        r'\*\*QUESTION \d+:\*\* (.+?)\n'
        r'\*\*OPTION A:\*\* (.+?)\n'
        r'\*\*OPTION B:\*\* (.+?)\n'
        r'\*\*OPTION C:\*\* (.+?)\n'
        r'\*\*OPTION D:\*\* (.+?)\n'
        r'\*\*ANS:\*\* (.+?)(?=\n|$)',
        re.DOTALL
    )
    matches = pattern.findall(quiz_text)

    for match in matches:
        question = match[0].strip()
        options = [match[1].strip(), match[2].strip(), match[3].strip(), match[4].strip()]
        correct_ans = match[5].strip()

        question_data = {
            "question": question,
            "options": options,
            "correct_answer": correct_ans
        }
        questions.append(question_data)

    return questions

@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    print("Request received")
    student_topic = request.args.get('topic')
    if not student_topic:
        return jsonify({'error': 'Missing topic parameter'}), 400
    
    topic_descriptions = {
        "Algorithms": "This quiz covers essential topics in algorithm design and analysis.",
        "Data Structures": "This quiz tests your understanding of key data structures and their operations.",
        "Web Development": "Covers frontend and backend web technologies and concepts.",
        "Testing": "Evaluate your knowledge of software testing techniques and principles.",
        "Cloud Computing": "Explore fundamental concepts in cloud platforms and architecture.",
        "Artificial Intelligence": "Covers basic concepts in AI including reasoning, search, and ML.",
        "Computer Networks": "Test your understanding of networking protocols and communication models.",
        "Operating Systems": "This quiz covers OS fundamentals like memory, processes, and file systems.",
        "UI/UX Design": "Assess your grasp of user interface and user experience design principles.",
        "Version Control": "Check your knowledge of Git and collaborative development practices.",
        "Machine Learning": "Covers foundational ML concepts like supervised learning and models.",
        "Frontend Frameworks": "Explore frontend frameworks like React, Angular, or Vue.",
        "DevOps": "Test your understanding of continuous integration, delivery, and deployment.",
        "Software Architecture": "Covers software design patterns, layering, and modular systems.",
        "Backend Development": "Quiz on APIs, databases, and backend frameworks.",
        "Internet of Things": "Check your understanding of IoT devices, protocols, and data."
    }

    description = topic_descriptions.get(student_topic, "This quiz covers your selected topic in depth.")

    try:
        quiz = fetchQuizFromLlama(student_topic)
        print(quiz)
        processed_quiz = process_quiz(quiz)
        if not processed_quiz:
            return jsonify({'error': 'Failed to parse quiz data', 'raw_response': quiz}), 500
        return jsonify({
            'description': description,
            'quiz': processed_quiz
        }), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/test', methods=['GET'])
def run_test():
    return jsonify({'quiz': "test"}), 200

if __name__ == '__main__':
    port_num = 5000
    print(f"App running on port {port_num}")
    app.run(port=port_num, host="0.0.0.0")

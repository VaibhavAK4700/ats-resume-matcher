# Resume ATS Matcher (NLP / RAG Pipeline)

A production-ready NLP application that analyzes resumes against job descriptions, calculates semantic similarity scores, and highlights missing keywords.

## 🚀 Live Demo
[Live Demo Link Coming Soon]

## 🏗️ Architecture & Pipeline
1. **Document Ingestion:** PDF/DOCX parsing and text extraction.
2. **Text Processing:** Tokenization, stop-word removal, and semantic chunking.
3. **Embedding Generation:** Vectorization using Sentence-Transformers / HuggingFace.
4. **Scoring Engine:** Cosine similarity calculation + LLM-based gap analysis.

## 🛠️ Tech Stack
* **Language:** Python 3.11+
* **Frameworks:** FastAPI / Streamlit, LangChain
* **NLP / Models:** Sentence-Transformers, spaCy, Ollama (Local) / OpenAI API
* **DevOps:** Docker, GitHub Actions (CI/CD)

## 🔒 Data Privacy & GDPR Compliance
* Runs on local open-source LLMs (via Ollama) to ensure zero third-party data transmission of personal user CVs.

## 🚦 How to Run Locally
```bash
git clone [https://github.com/VaibhavAK4700/resume-ats-matcher.git](https://github.com/VaibhavAK4700/resume-ats-matcher.git)
cd resume-ats-matcher
pip install -r requirements.txt
python main.py

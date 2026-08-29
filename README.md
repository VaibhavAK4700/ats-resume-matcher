# Resume ATS Matcher (NLP / Microservices Pipeline)

A production-ready microservice platform that analyzes resumes against job descriptions, calculates semantic similarity scores using vector space embeddings, and highlights missing technical competencies.

---

## 🏗️ System Architecture & Data Pipeline
1. **Document Ingestion:** Apache PDFBox and Apache Tika text extraction from multi-page PDF resumes inside Spring Boot.
2. **Text Processing & NLP:** Tokenization, stop-word filtering, and lemmatization via SpaCy (`en_core_web_sm`).
3. **Semantic Scoring:** TF-IDF vectorization and Cosine Similarity calculation via `scikit-learn`.
4. **Keyword Gap Analysis:** Set-theoretic missing keyword extraction filtered by Part-of-Speech (`NOUN` and `PROPN`).
5. **Persistence & Tailoring:** Spring Data JPA storage in PostgreSQL and real-time generation of optimized draft summaries.

---

## 🛠️ Tech Stack

| Domain | Technologies |
| :--- | :--- |
| **Frontend** | Tailwind CSS, HTML5 Drag & Drop API, Asynchronous Fetch JS |
| **Backend API** | Java 17, Spring Boot 3.2, Spring Data JPA, WebFlux (`WebClient`), Apache PDFBox |
| **AI / NLP Microservice** | Python 3.10, FastAPI, Uvicorn, spaCy, Scikit-Learn |
| **Database** | PostgreSQL 15 |
| **Containerization** | Docker, Docker Compose (Multi-stage builds, non-root security compliance) |

---

## 🔒 Data Privacy & GDPR Compliance

* **Local NLP Processing:** All text parsing, lemmatization, and similarity scoring are executed strictly within internal microservice containers—ensuring **zero third-party API data transmission** of candidate personal data.

---

## 🚦 How to Run Locally

### Option 1: Docker Compose Multi-Container Build (Recommended)

Make sure you have [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) installed.

```bash
# 1. Clone the repository
git clone [https://github.com/VaibhavAK4700/resume-ats-matcher.git](https://github.com/VaibhavAK4700/resume-ats-matcher.git)
cd resume-ats-matcher

# 2. Build and start PostgreSQL, Python AI service, and Spring Boot backend
docker compose up --build -d

# 3. Check container statuses
docker compose ps
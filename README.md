# 🤖 Automated ATS Resume Matcher & Job Aggregator

An intelligent, microservice-based Automated Tracking System (ATS) platform that scrapes live job postings, evaluates candidate fit via TF-IDF vectorization and SpaCy NLP analysis, and automatically dispatches ranked job digests via email.

---

## 🛠️ Tech Stack & Architecture

- **Backend Platform:** Java 17, Spring Boot 3, Spring Data JPA, Spring Mail
- **AI/NLP Microservice:** Python 3.11, FastAPI, `jobspy` (Multi-board Scraper), SpaCy, Scikit-Learn
- **Database:** PostgreSQL 16
- **Containerization:** Docker & Docker Compose

---

## 🚀 System Architecture
+-------------------------------+
                   |    Spring Boot Backend        |
                   |       (Port 8080)             |
                   +---------------+---------------+
                                   |
               1. Trigger Scrape   |   2. Post Payload
               & Fetch Jobs        v   to /analyze
                   +-------------------------------+
                   |     Python AI Microservice    |
                   |       (FastAPI @ 8000)        |
                   +---------------+---------------+
                                   |
               3. Evaluates Match  |  4. Returns Scores
               via TF-IDF / NLP    v  & Missing Skills
                   +-------------------------------+
                   |   Job Boards (LinkedIn, etc.) |
                   +---------------+---------------+
                                   |
               5. Dispatches Email |  6. Gmail SMTP
               Digest to Target    v
                   +-------------------------------------------+
                   |    notification,personal.mail@gmail.com   |
                   +-------------------------------------------+

---

## 💡 Key Features

* **Multi-Board Live Job Scraping:** Dynamically fetches active job postings across LinkedIn, Indeed, and Glassdoor without vendor API keys.
* **Semantic ATS Keyword Evaluation:** Uses TF-IDF cosine similarity and SpaCy lemmatization to extract domain-specific noun phrases and score candidate-to-job fit.
* **Automated Daily Email Digest:** Runs a Spring `@Scheduled` background job (09:00 AM daily) to evaluate new regional job postings and email ranked digests.
* **On-Demand REST Automation:** Exposes parameterized REST API endpoints to run real-time target searches for specific queries, radii, and volume targets.

---

## ⚡ Quick Start with Docker

### 1. Prerequisites
Ensure you have installed:
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- `git` & `curl`

### 2. Environment Configuration
Create an `.env` file or export your SMTP App Password credentials:

```bash
# Environment variables for Spring Mail
SPRING_MAIL_USERNAME=notification.personal.mail@gmail.com
ATS_NOTIFICATION_TARGET_EMAIL=notification.personal.mail@gmail.com


# Clone repository
git clone [https://github.com/your-username/resume-ats-matcher.git](https://github.com/your-username/resume-ats-matcher.git)
cd resume-ats-matcher

# Build and start services in detached mode
docker compose up --build -d
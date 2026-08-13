CREATE TABLE IF NOT EXISTS resumes (
    resume__id SERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    parsed_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS job_postings (
    job_posting__id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS matches_analyses (
    analysis__id SERIAL PRIMARY KEY,
    ats_score INT NOT NULL,
    matched_keywords TEXT,
    missing_keywords TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
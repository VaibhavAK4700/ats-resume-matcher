-- Database initialization for PostgreSQL ATS Platform

CREATE TABLE IF NOT EXISTS job_postings (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    location VARCHAR(255),
    job_url VARCHAR(1000) UNIQUE,
    source VARCHAR(100),
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_job_title ON job_postings(title);
CREATE INDEX IF NOT EXISTS idx_job_company ON job_postings(company_name);

CREATE TABLE IF NOT EXISTS resumes (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    storage_path VARCHAR(500),
    candidate_email VARCHAR(255),
    is_base_resume BOOLEAN NOT NULL DEFAULT FALSE,
    template_data BYTEA,
    extracted_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_base_resume ON resumes(is_base_resume);

CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGSERIAL PRIMARY KEY,
    resume_id BIGINT REFERENCES resumes(id) ON DELETE CASCADE,
    job_id BIGINT REFERENCES job_postings(id) ON DELETE CASCADE,
    match_score INT NOT NULL,
    reasoning TEXT,
    tailored_summary TEXT,
    tailored_bullets TEXT,
    tailored_resume_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS analysis_missing_keywords (
    analysis_id BIGINT REFERENCES analysis_results(id) ON DELETE CASCADE,
    keyword VARCHAR(255) NOT NULL,
    PRIMARY KEY (analysis_id, keyword)
);
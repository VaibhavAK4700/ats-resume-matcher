-- Insert Sample Resumes
INSERT INTO resumes (file_name, extracted_text, is_base_resume, created_at) 
VALUES 
('alice_backend_resume.pdf', 'Experienced Java Backend Developer proficient in Spring Boot, REST API design, PostgreSQL, Docker, Maven, and Git.', TRUE, CURRENT_TIMESTAMP),
('bob_frontend_resume.pdf', 'Frontend Software Engineer specializing in React, TypeScript, HTML5, CSS3, Tailwind, and Redux.', FALSE, CURRENT_TIMESTAMP);

-- Insert Sample Job Postings
INSERT INTO job_postings (title, company, description, created_at) 
VALUES 
('Senior Java Engineer', 'Tech Corp', 'Looking for a Java Engineer with deep knowledge of Spring Boot, Microservices, PostgreSQL, Kubernetes, Docker, and Redis.', CURRENT_TIMESTAMP),
('Fullstack Developer', 'Innovate LLC', 'Seeking a Fullstack Developer skilled in React, Node.js, TypeScript, PostgreSQL, AWS, and Docker.', CURRENT_TIMESTAMP);

-- Insert Sample Analysis Result (Linked to Resume ID 1 and Job Posting ID 1)
INSERT INTO analysis_results (resume_id, job_id, match_score, reasoning, tailored_resume_text, created_at) 
VALUES 
(1, 1, 73, 'Good core Java background. Missing cloud orchestration tools.', 'Tailored version of Alice''s resume emphasizing Spring Boot and PostgreSQL experience.', CURRENT_TIMESTAMP);

-- Insert Missing Keywords for Analysis ID 1
INSERT INTO analysis_missing_keywords (analysis_id, keyword) 
VALUES 
(1, 'kubernetes'),
(1, 'redis'),
(1, 'microservices');

INSERT INTO job_postings (title, company_name, description, created_at) 
VALUES 
  ('Senior Java Engineer', 'Tech Corp', 'Looking for a Java Engineer...', CURRENT_TIMESTAMP),
  ('Fullstack Developer', 'Innovate LLC', 'Seeking a Fullstack Developer...', CURRENT_TIMESTAMP);
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
import spacy
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from typing import List

app = FastAPI(title="ATS AI Service", version="1.0")

# Load SpaCy model for tokenization and lemmatization
try:
    nlp = spacy.load("en_core_web_sm")
except Exception:
    import en_core_web_sm
    nlp = en_core_web_sm.load()

class AnalysisRequest(BaseModel):
    # Field aliases match both snake_case (from Spring Boot WebClient) and camelCase
    resume_text: str = Field(..., alias="resumeText")
    job_description: str = Field(..., alias="jobDescription")

    class Config:
        populate_by_name = True

class AnalysisResponse(BaseModel):
    matchScore: int
    reasoning: str
    tailoredResumeText: str
    tailoredSummary: str
    tailoredBullets: str
    missingKeywords: List[str]

    class Config:
        populate_by_name = True

@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume(request: AnalysisRequest):
    try:
        resume_raw = request.resume_text
        job_raw = request.job_description

        # 1. TF-IDF Cosine Similarity Calculation
        vectorizer = TfidfVectorizer(stop_words="english")
        tfidf_matrix = vectorizer.fit_transform([resume_raw, job_raw])
        similarity_score = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])[0][0]
        match_score = int(round(similarity_score * 100))

        # 2. SpaCy Keyword Extraction & Missing Skill Identification
        doc_resume = nlp(resume_raw.lower())
        doc_job = nlp(job_raw.lower())

        job_keywords = {
            token.lemma_ for token in doc_job 
            if token.is_alpha and not token.is_stop and token.pos_ in ["NOUN", "PROPN"]
        }
        resume_keywords = {
            token.lemma_ for token in doc_resume 
            if token.is_alpha and not token.is_stop
        }
        
        missing = sorted(list(job_keywords - resume_keywords))[:5]
        missing_str = ", ".join(missing) if missing else "None"

        # 3. Generate Tailored Text Payload
        reasoning = f"Evaluated via TF-IDF semantic vectorization. Match score: {match_score}%. Missing core terms: {missing_str}."
        tailored_summary = f"Results-driven professional with key focus on {', '.join(list(job_keywords)[:3])}."
        tailored_bullets = f"• Integrated key domain capabilities aligned with target position.\n• Highlighted proficiency in {missing_str}."

        return AnalysisResponse(
            matchScore=match_score,
            reasoning=reasoning,
            tailoredResumeText=resume_raw,
            tailoredSummary=tailored_summary,
            tailoredBullets=tailored_bullets,
            missingKeywords=missing
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

@app.get("/health")
async def health_check():
    return {"status": "UP"}
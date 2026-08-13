from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from sentence_transformers import SentenceTransformer, util
import pypdf
import io
import re

app = FastAPI()

# Load model
model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')

# Common filler/action words to ignore during keyword extraction
STOPWORDS = {
    "looking", "for", "a", "an", "the", "with", "and", "or", "in", "at", 
    "to", "experience", "developer", "engineer", "required", "skills", 
    "strong", "knowledge", "working", "ability", "seeking", "experienced",
    "job", "description", "candidate", "role", "position", "proficient"
}

@app.post("/analyze")
async def analyze_resume(
    file: UploadFile = File(...),
    job_text: str = Form(
        ..., 
        description="Provide the job description text here",
        examples=["Looking for a Senior Software Engineer with Java and Python experience."]
    )
):
    try:
        # Read file stream ONCE
        contents = await file.read()
        resume_text = ""

        # Parse based on file extension
        if file.filename.lower().endswith(".pdf"):
            try:
                pdf_reader = pypdf.PdfReader(io.BytesIO(contents))
                for page in pdf_reader.pages:
                    extracted = page.extract_text()
                    if extracted:
                        resume_text += extracted + " "
            except Exception:
                raise HTTPException(status_code=400, detail="Uploaded PDF file is invalid or corrupted.")
        else:
            # Fallback for plain text files (.txt)
            resume_text = contents.decode("utf-8", errors="ignore")

        resume_text = resume_text.strip()
        
        # Validation check
        if not resume_text:
            return {"match_score": 0.0, "missing_keywords": []}

        # Calculate AI Semantic Similarity
        resume_embedding = model.encode(resume_text, convert_to_tensor=True)
        job_embedding = model.encode(job_text, convert_to_tensor=True)
        cosine_sim = util.cos_sim(resume_embedding, job_embedding).item()
        
        score = round(max(0.0, min(100.0, cosine_sim * 100)), 2)

        # Keyword Extraction & Matching
        job_words = set(re.findall(r'\b[a-zA-Z0-9+#.]+\b', job_text.lower()))
        required_keywords = [
            w for w in job_words 
            if w not in STOPWORDS and len(w) > 1 and not w.isdigit()
        ]

        resume_text_lower = resume_text.lower()
        missing = [kw for kw in required_keywords if kw not in resume_text_lower]

        return {
            "match_score": float(score),
            "missing_keywords": missing
        }

    except HTTPException as he:
        raise he
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
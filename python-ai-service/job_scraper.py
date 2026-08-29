# python-ai-service/job_scraper.py
from jobspy import scrape_jobs
import logging

def search_live_jobs(query: str, location: str, distance: int, max_results: int = 100):
    try:
        jobs = scrape_jobs(
            site_name=["indeed", "linkedin", "glassdoor"],
            search_term=query,
            location=location,
            distance=int(distance),
            results_wanted=int(max_results),
            country_housing="germany",
            fetch_description=True  # Force fetching full job descriptions
        )
        
        if jobs.empty:
            return []

        # Drop duplicates by title + company
        jobs = jobs.drop_duplicates(subset=['title', 'company'])

        # Fallback: fill empty descriptions with title and company so TF-IDF doesn't fail
        jobs['description'] = jobs['description'].replace('', None)
        jobs['description'] = jobs['description'].fillna(jobs['title'] + " " + jobs['company'])

        return jobs[['title', 'company', 'job_url', 'description']].to_dict(orient='records')

    except Exception as e:
        logging.error(f"Error scraping job listings: {e}")
        return []
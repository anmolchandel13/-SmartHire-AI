package com.smarthire.model;

/**
 * Enum representing user roles in the SmartHire AI platform.
 *
 * CANDIDATE — A job seeker who can:
 *   - Register and log in
 *   - Create/update their profile
 *   - Upload a resume PDF
 *   - Trigger AI analysis and view their scorecard
 *
 * ADMIN — An HR manager/recruiter who can:
 *   - Log in with admin privileges
 *   - View all registered candidates on a dashboard
 *   - Filter and sort candidates by score, branch, skills
 *   - Shortlist top candidates
 *   - Export shortlisted candidates as a PDF report
 *   - Send email notifications to shortlisted candidates
 *
 * This enum is stored as a STRING in the database (not an integer),
 * so the column will contain "CANDIDATE" or "ADMIN" — making it
 * human-readable directly in the database.
 */
public enum Role {
    CANDIDATE,
    ADMIN
}

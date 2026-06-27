/**
 * Service Layer — Business Logic.
 *
 * Services contain ALL the business rules and logic of the application.
 * They are where the "real work" happens. Services:
 *   - Are called by controllers
 *   - Call repositories to access the database
 *   - Call external APIs (Gemini AI, email service)
 *   - Perform data transformations and validations
 *
 * Each service is annotated with @Service, making it a Spring-managed bean.
 *
 * Key services:
 *   - AuthService:      User registration, login, token generation
 *   - ProfileService:   Candidate profile CRUD operations
 *   - ResumeService:    PDF upload, text extraction using PDFBox
 *   - AIService:        Gemini API integration, prompt building, response parsing
 *   - AdminService:     Candidate listing, filtering, shortlisting
 *   - EmailService:     Sending notification emails to shortlisted candidates
 */
package com.smarthire.service;

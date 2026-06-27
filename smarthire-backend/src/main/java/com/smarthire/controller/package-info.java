/**
 * Controller Layer — REST API Endpoints.
 *
 * Controllers are the "front door" of the backend. They:
 *   - Receive HTTP requests from the React frontend
 *   - Validate incoming data (with help from DTOs)
 *   - Delegate business logic to the Service layer
 *   - Return HTTP responses with appropriate status codes
 *
 * Controllers should NEVER contain business logic directly.
 * They are thin — their only job is to route requests to services.
 *
 * Endpoints organized by feature:
 *   - AuthController:      /api/auth/**      (register, login)
 *   - CandidateController: /api/candidate/**  (profile, resume, analysis)
 *   - AdminController:     /api/admin/**      (dashboard, shortlist, export, notify)
 */
package com.smarthire.controller;

/**
 * DTO Layer — Data Transfer Objects.
 *
 * DTOs are simple classes that define the SHAPE of data going into and
 * out of our API. They serve as a protective layer between the outside
 * world (frontend) and our internal entities (database tables).
 *
 * Why not just use Entity classes directly?
 *   1. SECURITY: We never want to expose the password field in a response
 *   2. FLEXIBILITY: API response shape can differ from database structure
 *   3. VALIDATION: DTOs carry @NotBlank, @Email, @Size annotations for input validation
 *   4. DECOUPLING: Changes to the database don't automatically break the API
 *
 * Sub-packages:
 *   - request/  — DTOs for incoming request bodies (RegisterRequest, LoginRequest, etc.)
 *   - response/ — DTOs for outgoing response bodies (AuthResponse, ScorecardResponse, etc.)
 */
package com.smarthire.dto;

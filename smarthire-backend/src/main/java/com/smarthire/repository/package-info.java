/**
 * Repository Layer — Database Access.
 *
 * Repositories are interfaces that extend Spring Data JPA's JpaRepository.
 * They provide ready-made methods for database operations without writing SQL:
 *   - save(), findById(), findAll(), deleteById() — provided automatically
 *   - Custom query methods using Spring Data naming conventions
 *     (e.g., findByEmail() generates the SQL for us)
 *
 * Each repository maps to one entity (database table):
 *   - UserRepository    → Users table
 *   - ProfileRepository → Profiles table
 *   - ResumeRepository  → Resumes table
 *   - AIResultRepository → AI_Results table
 */
package com.smarthire.repository;

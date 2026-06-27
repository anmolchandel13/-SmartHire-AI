/**
 * Model Layer — JPA Entity Classes.
 *
 * Each class in this package maps directly to a MySQL database table.
 * JPA annotations (@Entity, @Table, @Column) tell Hibernate how to
 * translate between Java objects and database rows.
 *
 * Entity classes:
 *   - User:     Maps to 'users' table     — login credentials, roles
 *   - Profile:  Maps to 'profiles' table   — candidate personal/academic info
 *   - Resume:   Maps to 'resumes' table    — uploaded PDF path, extracted text
 *   - AIResult: Maps to 'ai_results' table — AI-generated scorecard data
 *
 * Also contains the Role enum (CANDIDATE, ADMIN) for type-safe role handling.
 */
package com.smarthire.model;

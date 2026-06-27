/**
 * Exception Layer — Global Error Handling.
 *
 * This package provides a centralized way to handle all errors in the application.
 * Instead of each controller handling its own errors, we use a
 * @RestControllerAdvice class that catches exceptions globally and returns
 * consistent, well-formatted error responses.
 *
 * Components:
 *   - GlobalExceptionHandler: Catches all exceptions and maps them to HTTP responses
 *   - ResourceNotFoundException: Thrown when a requested resource doesn't exist (404)
 *   - BadRequestException: Thrown when the request data is invalid (400)
 *   - UnauthorizedException: Thrown when authentication fails (401)
 *   - ErrorResponse: Standardized error response DTO with status, message, and timestamp
 */
package com.smarthire.exception;

package com.smarthire.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for simple success/error messages.
 *
 * Used when we need to return just a message without complex data,
 * for example after a successful file upload or profile update.
 *
 * Example:
 * {
 *     "success": true,
 *     "message": "Resume uploaded and text extracted successfully"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse {

    private Boolean success;
    private String message;
}

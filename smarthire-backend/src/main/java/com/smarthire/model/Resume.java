package com.smarthire.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Resume Entity — Maps to the 'resumes' table in MySQL.
 *
 * This table stores information about uploaded resume PDFs.
 * We store TWO things:
 *   1. filePath:      The filesystem path where the actual PDF file is saved
 *   2. extractedText: The plain text content extracted from the PDF using Apache PDFBox
 *
 * Why store extracted text separately?
 *   - The AI service needs plain text, not a PDF binary
 *   - We can display the text content on the frontend without a PDF viewer
 *   - We avoid re-extracting text from the PDF every time (performance)
 *   - The text can be searched/indexed if we ever add search functionality
 *
 * Relationships:
 *   - Belongs to one Profile (via @OneToOne with @JoinColumn)
 *
 * Design Decisions:
 *   - @Lob + @Column(columnDefinition = "LONGTEXT"): Resume text can be very long
 *     (multiple pages), LONGTEXT in MySQL supports up to 4GB of text
 *   - filePath stores a relative path (./uploads/resumes/filename.pdf)
 */
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owning side of the Profile ↔ Resume relationship.
     * The 'profile_id' FK column is created in the resumes table.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    /**
     * Filesystem path where the uploaded PDF is stored.
     * Example: "./uploads/resumes/1_john_doe_resume.pdf"
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * The full plain text extracted from the PDF resume using Apache PDFBox.
     * This is what gets sent to the AI for analysis.
     * LONGTEXT supports up to 4GB — more than enough for any resume.
     */
    @Lob
    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    /**
     * Original filename of the uploaded PDF (for display purposes).
     * Example: "John_Doe_Resume_2024.pdf"
     */
    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}

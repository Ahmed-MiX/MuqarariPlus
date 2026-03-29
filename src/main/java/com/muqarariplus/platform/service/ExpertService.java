package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.entity.ExpertStatus;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.ExpertRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ExpertService {

    private static final String UPLOAD_DIR = "uploads/cv/";
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".pdf", ".docx", ".doc");

    private final ExpertRepository expertRepository;
    private final UserRepository userRepository;

    public ExpertService(ExpertRepository expertRepository, UserRepository userRepository) {
        this.expertRepository = expertRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lazily creates an Expert entity if one doesn't exist for the given user.
     */
    @Transactional
    public Expert getOrCreateExpert(User user) {
        Optional<Expert> existing = expertRepository.findByUserId(user.getId());
        if (existing.isPresent()) {
            return existing.get();
        }
        Expert expert = new Expert();
        expert.setUser(user);
        expert.setStatus(ExpertStatus.NONE);
        expert.setRating(0.0);
        return expertRepository.save(expert);
    }

    /**
     * Retrieves expert by user email.
     */
    public Optional<Expert> getExpertByEmail(String email) {
        return expertRepository.findByUserEmail(email);
    }

    /**
     * Retrieves expert by user ID.
     */
    public Optional<Expert> getExpertById(Long id) {
        return expertRepository.findById(id);
    }

    /**
     * Returns all experts with PENDING status for admin review.
     */
    public List<Expert> getPendingExperts() {
        return expertRepository.findByStatus(ExpertStatus.PENDING);
    }

    /**
     * Returns all experts with APPROVED status.
     */
    public List<Expert> getApprovedExperts() {
        return expertRepository.findByStatus(ExpertStatus.APPROVED);
    }

    /**
     * Handles identity verification submission:
     * 1. Validates file extension (.pdf, .docx, .doc only)
     * 2. Saves file to local storage under uploads/cv/
     * 3. Updates Expert entity with file path, LinkedIn URL, and PENDING status
     */
    @Transactional
    public void submitVerification(User user, MultipartFile cvFile, String linkedinUrl) throws IOException {
        Expert expert = getOrCreateExpert(user);

        // Validate file extension
        String originalFilename = cvFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("CV file name is empty.");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, DOCX, and DOC files are allowed.");
        }

        // Ensure upload directory exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename: {userId}_{timestamp}_{originalName}
        String storedFilename = user.getId() + "_" + System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(cvFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update Expert entity
        expert.setCvFilePath(storedFilename);
        expert.setCvUrl("/uploads/cv/" + storedFilename);
        expert.setLinkedinUrl(linkedinUrl);
        expert.setStatus(ExpertStatus.PENDING);
        expert.setLastSubmissionTime(LocalDateTime.now());
        expertRepository.save(expert);
    }

    /**
     * Admin approves an expert — sets status to APPROVED.
     */
    @Transactional
    public void approveExpert(Long expertId) {
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new IllegalArgumentException("Expert not found with ID: " + expertId));
        expert.setStatus(ExpertStatus.APPROVED);
        expertRepository.save(expert);

        // Also update the User status to APPROVED for consistency
        User user = expert.getUser();
        user.setStatus("APPROVED");
        userRepository.save(user);
    }

    /**
     * Admin rejects an expert — sets status to REJECTED and stamps the time for cooldown.
     */
    @Transactional
    public void rejectExpert(Long expertId) {
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new IllegalArgumentException("Expert not found with ID: " + expertId));
        expert.setStatus(ExpertStatus.REJECTED);
        expert.setLastSubmissionTime(LocalDateTime.now());
        expertRepository.save(expert);
    }

    /**
     * Calculates remaining cooldown seconds for a REJECTED expert.
     * Returns 0 if the cooldown has expired or expert is not in REJECTED status.
     */
    public long getCooldownRemainingSeconds(Expert expert) {
        if (expert.getStatus() != ExpertStatus.REJECTED || expert.getLastSubmissionTime() == null) {
            return 0;
        }
        LocalDateTime cooldownEnd = expert.getLastSubmissionTime().plusMinutes(5);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(cooldownEnd)) {
            return 0;
        }
        return java.time.Duration.between(now, cooldownEnd).getSeconds();
    }
}

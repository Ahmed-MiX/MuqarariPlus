package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EngagementService {

    private final CourseEnrichmentRepository enrichmentRepository;
    private final UserRepository userRepository;

    public EngagementService(CourseEnrichmentRepository enrichmentRepository,
                             UserRepository userRepository) {
        this.enrichmentRepository = enrichmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Toggle upvote: if the user already upvoted, remove it; otherwise, add it.
     * Returns the new upvote count after the toggle.
     */
    @Transactional
    public int toggleUpvote(Long enrichmentId, String userIdentifier) {
        CourseEnrichment enrichment = enrichmentRepository.findById(enrichmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrichment not found: " + enrichmentId));
        User user = resolveUser(userIdentifier);

        if (enrichment.isUpvotedBy(user)) {
            enrichment.removeUpvote(user);
        } else {
            enrichment.addUpvote(user);
        }

        enrichmentRepository.save(enrichment);
        return enrichment.getUpvoteCount();
    }

    /**
     * Toggle bookmark: if the user already bookmarked, remove it; otherwise, add it.
     * Returns true if bookmarked after toggle, false if un-bookmarked.
     */
    @Transactional
    public boolean toggleBookmark(Long enrichmentId, String userIdentifier) {
        CourseEnrichment enrichment = enrichmentRepository.findById(enrichmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrichment not found: " + enrichmentId));
        User user = resolveUser(userIdentifier);

        if (enrichment.isBookmarkedBy(user)) {
            enrichment.removeBookmark(user);
            enrichmentRepository.save(enrichment);
            return false;
        } else {
            enrichment.addBookmark(user);
            enrichmentRepository.save(enrichment);
            return true;
        }
    }

    /**
     * Returns all enrichments bookmarked by a specific user.
     */
    @Transactional(readOnly = true)
    public List<CourseEnrichment> getBookmarkedEnrichments(String userIdentifier) {
        User user = resolveUser(userIdentifier);
        // Get all enrichments and filter those bookmarked by this user
        return enrichmentRepository.findAll().stream()
                .filter(e -> e.isBookmarkedBy(user))
                .collect(Collectors.toList());
    }

    /**
     * Resolves User from login identifier (email or username).
     */
    private User resolveUser(String identifier) {
        User user = userRepository.findByEmail(identifier);
        if (user == null) {
            user = userRepository.findByUsername(identifier);
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + identifier);
        }
        return user;
    }
}

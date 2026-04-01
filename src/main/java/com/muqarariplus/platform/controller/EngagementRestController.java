package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.service.EngagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/engagement")
public class EngagementRestController {

    private final EngagementService engagementService;

    public EngagementRestController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    /**
     * POST /api/engagement/{id}/upvote
     * Toggles the upvote for the current user on the given enrichment.
     * Returns JSON: { "upvoteCount": N, "action": "upvoted" | "removed" }
     */
    @PostMapping("/{id}/upvote")
    public ResponseEntity<Map<String, Object>> toggleUpvote(@PathVariable("id") Long enrichmentId,
                                                            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        int newCount = engagementService.toggleUpvote(enrichmentId, principal.getName());

        return ResponseEntity.ok(Map.of(
                "upvoteCount", newCount,
                "status", "success"
        ));
    }

    /**
     * POST /api/engagement/{id}/bookmark
     * Toggles the bookmark for the current user on the given enrichment.
     * Returns JSON: { "bookmarked": true|false, "status": "success" }
     */
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(@PathVariable("id") Long enrichmentId,
                                                              Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        boolean bookmarked = engagementService.toggleBookmark(enrichmentId, principal.getName());

        return ResponseEntity.ok(Map.of(
                "bookmarked", bookmarked,
                "status", "success"
        ));
    }
}

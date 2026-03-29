package com.muqarariplus.platform.entity;

/**
 * Represents the verification lifecycle of an Expert on the platform.
 * NONE     → Initial state, expert has not submitted verification documents.
 * PENDING  → Expert submitted CV + LinkedIn, awaiting admin review.
 * APPROVED → Admin verified the expert. Full platform access granted.
 * REJECTED → Admin rejected the submission. Cooldown timer applies.
 */
public enum ExpertStatus {
    NONE,
    PENDING,
    APPROVED,
    REJECTED
}

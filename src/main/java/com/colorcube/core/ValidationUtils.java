package com.colorcube.core;

import java.util.HashMap;
import java.util.Map;

public class ValidationUtils {

    public static ValidationResult validateState(String facelets) {
        if (facelets == null || facelets.length() != 54) {
            return new ValidationResult(false, "Invalid state length. Must be 54 facelets.");
        }

        // 1. Check Color Counts
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : facelets.toCharArray()) {
            counts.put(c, counts.getOrDefault(c, 0) + 1);
        }

        for (char c : "WRGYOB".toCharArray()) {
            // Assuming standard colors. If user uses different chars, we might need to be
            // flexible.
            // But specs say "U,R,F,D,L,B" or color letters. Let's assume the model uses
            // standard chars.
            // If the model uses U,R,F... we should check those.
            // The CubeModel uses "WRGYOB" in SOLVED_STATE.
            if (counts.getOrDefault(c, 0) != 9) {
                return new ValidationResult(false,
                        "Incorrect count for color " + c + ". Expected 9, found " + counts.getOrDefault(c, 0));
            }
        }

        // 2. Check Centers (Fixed)
        // Centers are at indices: 4, 13, 22, 31, 40, 49
        // U(4)=W, R(13)=R, F(22)=G, D(31)=Y, L(40)=O, B(49)=B
        if (facelets.charAt(4) != 'W')
            return new ValidationResult(false, "Center U must be White (W).");
        if (facelets.charAt(13) != 'R')
            return new ValidationResult(false, "Center R must be Red (R).");
        if (facelets.charAt(22) != 'G')
            return new ValidationResult(false, "Center F must be Green (G).");
        if (facelets.charAt(31) != 'Y')
            return new ValidationResult(false, "Center D must be Yellow (Y).");
        if (facelets.charAt(40) != 'O')
            return new ValidationResult(false, "Center L must be Orange (O).");
        if (facelets.charAt(49) != 'B')
            return new ValidationResult(false, "Center B must be Blue (B).");

        // 3. Advanced Parity Checks (Optional but recommended)
        // Edge parity, Corner parity, Permutation parity.
        // This is complex to implement fully without a solver library.
        // For MVP, color count is the most critical.

        return new ValidationResult(true, "Valid");
    }

    public static class ValidationResult {
        public final boolean valid;
        public final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }
}

package com.sjmt.SJMT.Config;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TemporaryPassword {

    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS  = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL     = UPPER + LOWER + DIGITS + SPECIAL;

    private static final int LENGTH = 16;

    /**
     * B-C2/B-C3 (Option A): Generates a cryptographically strong 16-character
     * temporary password (≈95 bits of entropy) with guaranteed presence of at
     * least one uppercase letter, one lowercase letter, one digit, and one
     * special character to satisfy common password policies.
     */
    public String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        List<Character> chars = new ArrayList<>(LENGTH);

        // Guarantee one character from each required category
        chars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Fill the remaining positions from the full character set
        for (int i = 4; i < LENGTH; i++) {
            chars.add(ALL.charAt(random.nextInt(ALL.length())));
        }

        // Shuffle so the guaranteed characters aren't always at fixed positions
        Collections.shuffle(chars, random);

        StringBuilder password = new StringBuilder(LENGTH);
        for (char c : chars) {
            password.append(c);
        }
        return password.toString();
    }
}

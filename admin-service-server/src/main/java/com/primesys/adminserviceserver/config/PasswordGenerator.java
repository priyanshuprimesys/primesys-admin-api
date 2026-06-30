package com.primesys.adminserviceserver.config;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordGenerator {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomPassword() {
        StringBuilder password = new StringBuilder(8);

        // Add 4 random lowercase letters
        for (int i = 0; i < 4; i++) {
            password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        }

        // Add 4 random digits
        for (int i = 0; i < 4; i++) {
            password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }

        // Shuffle the characters to avoid predictable patterns
        return shuffleString(password.toString());
    }

    private static String shuffleString(String input) {
        List<Character> characters = input.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(Collections.singletonList(characters), RANDOM);
        StringBuilder result = new StringBuilder(characters.size());
        for (char c : characters) {
            result.append(c);
        }
        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println(generateRandomPassword());
    }
}

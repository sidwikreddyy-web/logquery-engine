package com.sidwik.logquery.logs;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KeywordTokenizer {
    public Set<String> tokenize(String message) {
        if (message == null || message.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(message.toLowerCase().split("[^a-z0-9]+"))
                .filter(term -> term.length() >= 3)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

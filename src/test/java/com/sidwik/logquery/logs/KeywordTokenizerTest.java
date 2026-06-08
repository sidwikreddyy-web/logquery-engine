package com.sidwik.logquery.logs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordTokenizerTest {
    private final KeywordTokenizer tokenizer = new KeywordTokenizer();

    @Test
    void extractsLowercaseSearchTerms() {
        assertThat(tokenizer.tokenize("Payment API failed with TIMEOUT error"))
                .containsExactly("payment", "api", "failed", "with", "timeout", "error");
    }

    @Test
    void removesShortTermsAndDuplicates() {
        assertThat(tokenizer.tokenize("DB db db latency p99 latency"))
                .containsExactly("latency", "p99");
    }
}

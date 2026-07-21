package com.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urlshortener.config.AppProperties;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {

    private final AppProperties props = new AppProperties();
    private final ShortCodeGenerator gen = new ShortCodeGenerator(props);

    @Test
    void producesCodesOfConfiguredLength() {
        for (int i = 0; i < 200; i++) {
            assertThat(gen.generate()).hasSize(props.getShortCode().getLength());
        }
    }

    @Test
    void producesUrlSafeCodes() {
        Pattern p = Pattern.compile("^[A-Za-z0-9]+$");
        for (int i = 0; i < 200; i++) {
            String code = gen.generate();
            assertThat(p.matcher(code).matches())
                    .as("code %s should be Base62", code)
                    .isTrue();
        }
    }

    @Test
    void producesDistinctCodesInPractice() {
        Set<String> seen = new HashSet<>();
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            seen.add(gen.generate());
        }
        assertThat(seen).hasSize(n);
    }

    @Test
    void rejectsSillyLengths() {
        assertThatThrownBy(() -> gen.generate(2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> gen.generate(100)).isInstanceOf(IllegalArgumentException.class);
    }
}

package com.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urlshortener.config.AppProperties;
import com.urlshortener.web.error.AliasReservedException;
import com.urlshortener.web.error.InvalidUrlException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AliasPolicyTest {

    private final AppProperties props = props();
    private final AliasPolicy policy = new AliasPolicy(props);

    private AppProperties props() {
        AppProperties p = new AppProperties();
        p.setReservedPaths(Set.of("shorten", "stats", "health", "actuator", "error"));
        return p;
    }

    @Test
    void acceptsValidAlias() {
        assertThatCode(() -> policy.validate("my-alias_1")).doesNotThrowAnyException();
        assertThatCode(() -> policy.validate("abc")).doesNotThrowAnyException();
    }

    @Test
    void rejectsShort() {
        assertThatThrownBy(() -> policy.validate("ab"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsLong() {
        assertThatThrownBy(() -> policy.validate("a".repeat(33)))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsIllegalCharacters() {
        assertThatThrownBy(() -> policy.validate("has space"))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> policy.validate("has/slash"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsReservedRegardlessOfCase() {
        assertThatThrownBy(() -> policy.validate("shorten"))
                .isInstanceOf(AliasReservedException.class);
        assertThatThrownBy(() -> policy.validate("STATS"))
                .isInstanceOf(AliasReservedException.class);
    }

    @Test
    void isReservedIsCaseInsensitive() {
        assertThat(policy.isReserved("Health")).isTrue();
        assertThat(policy.isReserved("brand")).isFalse();
    }
}

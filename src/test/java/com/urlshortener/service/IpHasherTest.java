package com.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urlshortener.config.AppProperties;
import org.junit.jupiter.api.Test;

class IpHasherTest {

    private final AppProperties props = props("salt-a");
    private final IpHasher hasher = new IpHasher(props);

    private AppProperties props(String salt) {
        AppProperties p = new AppProperties();
        p.setIpHashSalt(salt);
        return p;
    }

    @Test
    void nullOrBlankReturnsNull() {
        assertThat(hasher.hash(null)).isNull();
        assertThat(hasher.hash("")).isNull();
        assertThat(hasher.hash("   ")).isNull();
    }

    @Test
    void isDeterministicForSameSalt() {
        String h1 = hasher.hash("1.2.3.4");
        String h2 = hasher.hash("1.2.3.4");
        assertThat(h1).isEqualTo(h2).hasSize(64);
    }

    @Test
    void differsForDifferentSalts() {
        IpHasher other = new IpHasher(props("salt-b"));
        assertThat(hasher.hash("1.2.3.4")).isNotEqualTo(other.hash("1.2.3.4"));
    }

    @Test
    void differsForDifferentIps() {
        assertThat(hasher.hash("1.2.3.4")).isNotEqualTo(hasher.hash("1.2.3.5"));
    }
}

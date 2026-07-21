package com.urlshortener;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlShortenerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Test
    void shortensAndRedirectsRoundTrip() throws Exception {
        String code = shorten("https://example.com/hello").get("code").asText();

        mvc.perform(get("/{code}", code))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "https://example.com/hello"));
    }

    @Test
    void unknownCodeReturns404() throws Exception {
        mvc.perform(get("/{code}", "abc1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", equalTo(404)))
                .andExpect(jsonPath("$.error", equalTo("not_found")));
    }

    @Test
    void codeShorterThanAllowedIsNotAShortenerRoute() throws Exception {
        mvc.perform(get("/ab"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sameUrlReturnsSameCode() throws Exception {
        String code1 = shorten("https://example.com/dup").get("code").asText();
        String code2 = shorten("https://example.com/dup").get("code").asText();
        org.assertj.core.api.Assertions.assertThat(code2).isEqualTo(code1);
    }

    @Test
    void urlNormalizationTreatsTrailingSlashAndDefaultPortAsSame() throws Exception {
        String c1 = shorten("https://example.com/norm").get("code").asText();
        String c2 = shorten("https://EXAMPLE.com:443/norm/").get("code").asText();
        org.assertj.core.api.Assertions.assertThat(c2).isEqualTo(c1);
    }

    @Test
    void rejectsMalformedUrl() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"not-a-url\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", equalTo("invalid_url")));
    }

    @Test
    void rejectsNonHttpScheme() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", equalTo("invalid_url")));
    }

    @Test
    void rejectsBlankBody() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingBody() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", equalTo("malformed_request")));
    }

    @Test
    void customAliasIsUsedAsCode() throws Exception {
        JsonNode body = shorten("https://example.com/brand", "brand-1");
        org.assertj.core.api.Assertions.assertThat(body.get("code").asText()).isEqualTo("brand-1");
        org.assertj.core.api.Assertions.assertThat(body.get("customAlias").asBoolean()).isTrue();

        mvc.perform(get("/brand-1"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "https://example.com/brand"));
    }

    @Test
    void aliasConflictReturns409() throws Exception {
        shorten("https://example.com/a", "conflict");
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/b\",\"alias\":\"conflict\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", equalTo("alias_conflict")));
    }

    @Test
    void aliasReservedReturns400() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/z\",\"alias\":\"stats\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", equalTo("alias_reserved")));
    }

    @Test
    void aliasWithInvalidCharactersRejected() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/z\",\"alias\":\"has space\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reshorteningSameUrlUnderSameAliasIsIdempotent() throws Exception {
        JsonNode a = shorten("https://example.com/same", "same-a");
        JsonNode b = shorten("https://example.com/same", "same-a");
        org.assertj.core.api.Assertions.assertThat(a.get("code").asText()).isEqualTo(b.get("code").asText());
    }

    @Test
    void redirectRecordsClickAndStatsReflectIt() throws Exception {
        String code = shorten("https://example.com/analytics").get("code").asText();

        mvc.perform(get("/{code}", code)
                        .header("User-Agent", "Mozilla/5.0")
                        .header("Referer", "https://ref.example/x"))
                .andExpect(status().isMovedPermanently());
        mvc.perform(get("/{code}", code)
                        .header("User-Agent", "curl/8")
                        .header("Referer", "https://ref.example/x"))
                .andExpect(status().isMovedPermanently());

        mvc.perform(get("/stats/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(code)))
                .andExpect(jsonPath("$.totalClicks", equalTo(2)))
                .andExpect(jsonPath("$.uniqueVisitors", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.topReferrers[0].label", equalTo("https://ref.example/x")))
                .andExpect(jsonPath("$.topReferrers[0].count", equalTo(2)))
                .andExpect(jsonPath("$.clicksByDay", hasSize(14)))
                .andExpect(jsonPath("$.firstClickAt", notNullValue()))
                .andExpect(jsonPath("$.lastClickAt", notNullValue()));
    }

    @Test
    void recentClicksEndpointPaginates() throws Exception {
        String code = shorten("https://example.com/clicks").get("code").asText();
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/{code}", code).header("User-Agent", "ua-" + i))
                    .andExpect(status().isMovedPermanently());
        }
        mvc.perform(get("/stats/{code}/clicks", code).param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void statsUnknownCodeReturns404() throws Exception {
        mvc.perform(get("/stats/{code}", "nosuch1"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/stats/{code}/clicks", "nosuch1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthReturnsOk() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("ok")));
    }

    private JsonNode shorten(String url) throws Exception {
        return shorten(url, null);
    }

    private JsonNode shorten(String url, String alias) throws Exception {
        String body = alias == null
                ? "{\"url\":\"" + url + "\"}"
                : "{\"url\":\"" + url + "\",\"alias\":\"" + alias + "\"}";
        MvcResult result = mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString());
    }
}

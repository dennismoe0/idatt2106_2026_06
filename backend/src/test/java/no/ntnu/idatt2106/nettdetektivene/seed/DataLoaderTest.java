package no.ntnu.idatt2106.nettdetektivene.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataLoaderTest {

    private final DataLoader dataLoader = new DataLoader(
        null,
        null,
        null,
        new ObjectMapper()
    );

    @Test
    void fakeNewsCorrectAnswerJson_derivesAnswersFromArticleIsRealFlags() {
        String result = dataLoader.fakeNewsCorrectAnswerJson("""
            {
              "articles": [
                { "headline": "A", "isReal": true },
                { "headline": "B", "isReal": false },
                { "headline": "C", "isReal": true }
              ]
            }
            """);

        assertThat(result).isEqualTo("{\"article_0\":true,\"article_1\":false,\"article_2\":true}");
    }

    @Test
    void fakeNewsCorrectAnswerJson_missingArticlesArray_throwsClearException() {
        assertThatThrownBy(() -> dataLoader.fakeNewsCorrectAnswerJson("{\"slides\":[]}"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("missing an 'articles' array");
    }
}

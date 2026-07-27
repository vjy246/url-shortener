package com.example.urlshortener.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AliasGeneratorTest {

    @Test
    void generatesAliasOfConfiguredLength() {
        AliasGenerator generator = new AliasGenerator(7);
        String alias = generator.generate();
        assertThat(alias).hasSize(7);
    }

    @Test
    void generatedAliasContainsOnlyBase62Characters() {
        AliasGenerator generator = new AliasGenerator(10);
        for (int i = 0; i < 100; i++) {
            String alias = generator.generate();
            assertThat(alias).matches("[0-9A-Za-z]+");
        }
    }

    @Test
    void generatesUniqueAliasesOverMultipleCalls() {
        AliasGenerator generator = new AliasGenerator(7);
        String first = generator.generate();
        String second = generator.generate();
        // statistically almost impossible to collide on two 7-char Base62 codes
        assertThat(first).isNotEqualTo(second);
    }
}

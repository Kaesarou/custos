package io.custos.node.adapters.out.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RomanNodeIdGeneratorTest {

    @Test
    void shouldGenerateStableRomanNodeIdFromAddress() {
        RomanNodeNameProvider nameProvider = new RomanNodeNameProvider() {
            @Override
            public List<String> names() {
                return List.of("Cassius", "Aurelius", "Hadrian");
            }
        };

        RomanNodeIdGenerator generator = new RomanNodeIdGenerator(nameProvider);

        String first = generator.generateFromAddress("0x70997970C51812dc3A010C7d01b50e0d17dc79C8");
        String second = generator.generateFromAddress("0x70997970C51812dc3A010C7d01b50e0d17dc79C8");

        assertEquals(first, second);
        assertTrue(first.matches("(Cassius|Aurelius|Hadrian)-70997970"));
    }
}
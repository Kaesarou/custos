package io.custos.node.adapters.out.security;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RomanNodeIdGenerator {

    private final RomanNodeNameProvider romanNodeNameProvider;

    public RomanNodeIdGenerator(RomanNodeNameProvider romanNodeNameProvider) {
        this.romanNodeNameProvider = romanNodeNameProvider;
    }

    public String generateFromAddress(String nodeAddress) {
        if (nodeAddress == null || nodeAddress.isBlank()) {
            throw new IllegalArgumentException("nodeAddress is required");
        }

        String normalizedAddress = nodeAddress.toLowerCase();

        List<String> names = romanNodeNameProvider.names();

        int index = Math.floorMod(normalizedAddress.hashCode(), names.size());
        String romanName = names.get(index);

        String suffix = normalizedAddress
                .replace("0x", "")
                .substring(0, 8);

        return romanName + "-" + suffix;
    }
}
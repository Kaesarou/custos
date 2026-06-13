package io.custos.node.adapters.out.security;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RomanNodeNameProvider {

    private static final String RESOURCE_PATH = "roman-node-names.txt";

    private final List<String> names;

    public RomanNodeNameProvider() {
        this.names = loadNames();
    }

    public List<String> names() {
        return names;
    }

    private List<String> loadNames() {
        try {
            ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            )) {
                List<String> loadedNames = reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isBlank())
                        .filter(line -> !line.startsWith("#"))
                        .distinct()
                        .toList();

                if (loadedNames.isEmpty()) {
                    throw new IllegalStateException("No roman node name configured");
                }

                return loadedNames;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load roman node names", e);
        }
    }
}
package com.example.docMind.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "chapter.detection")
@Data
public class ChapterPatternProperties {
    private List<PatternConfig> patterns = new ArrayList<>();

    @Data
    public static class PatternConfig {
        private String regex;
        private String label;      // e.g., "Chapter", "Section", "Part"
        private String language;   // For logging/debugging
    }
}

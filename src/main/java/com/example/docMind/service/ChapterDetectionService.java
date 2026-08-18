package com.example.docMind.service;

import com.example.docMind.config.ChapterPatternProperties;
import com.example.docMind.entity.Chapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ChapterDetectionService {

    private final List<ChapterPatternProperties.PatternConfig> patterns;

    public ChapterDetectionService(ChapterPatternProperties chapterPatternProperties) {
        this.patterns = chapterPatternProperties.getPatterns();
        log.info("Loaded {} chapter detection patterns.", patterns.size());
    }

    /**
     * Detects chapters using externalized patterns from application.yml.
     * Tries each pattern until one finds at least 2 matches.
     * Falls back to treating the whole text as one "chapter" if none match.
     */
    public List<Chapter> detectChapters(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            log.warn("Empty text provided.");
            return List.of();
        }

        // Try each pattern until we find one that splits the text into multiple parts
        for (ChapterPatternProperties.PatternConfig config : patterns) {
            List<Chapter> result = splitByPattern(fullText, config.getRegex(), config.getLabel());
            if (result.size() > 1) {
                log.info("Using pattern '{}' (language: {}) – found {} chapters.",
                        config.getLabel(), config.getLanguage(), result.size());
                return result;
            }
        }

        // No pattern found at least 2 chapters → treat whole text as a single chapter
        log.info("No chapter markers detected. Treating the whole text as a single chapter.");
        return List.of(Chapter.builder()
                .chapterIndex(1)
                .title("Full Document")
                .content(fullText)
                .build());
    }

    // --------------------------------------------------------------
    // Private helper: splits text using a single regex pattern
    // --------------------------------------------------------------
    private List<Chapter> splitByPattern(String text, String regex, String labelPrefix) {
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        // Find all positions where chapters start
        List<Integer> positions = new ArrayList<>();
        while (matcher.find()) {
            positions.add(matcher.start());
        }

        if (positions.isEmpty()) {
            return List.of();
        }

        List<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i);
            int end = (i + 1 < positions.size()) ? positions.get(i + 1) : text.length();

            String chapterContent = text.substring(start, end).trim();

            // Extract title: use the first line or fallback to "Section X"
            String title = extractTitle(chapterContent, labelPrefix, i + 1);

            Chapter chapter = Chapter.builder()
                    .chapterIndex(i + 1)
                    .title(title)
                    .content(chapterContent)
                    .build();

            chapters.add(chapter);
        }

        return chapters;
    }

    private String extractTitle(String content, String prefix, int index) {
        // Try to get the first non-empty line (up to 200 chars)
        int newline = content.indexOf('\n');
        if (newline > 0 && newline < 200) {
            String firstLine = content.substring(0, newline).trim();
            if (!firstLine.isEmpty()) {
                return firstLine;
            }
        }
        // Fallback: use a generic title
        return prefix + " " + index;
    }
}

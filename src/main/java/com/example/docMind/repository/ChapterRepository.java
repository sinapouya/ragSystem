package com.example.docMind.repository;

import com.example.docMind.entity.Chapter;
import com.example.docMind.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByDocumentOrderByChapterIndexAsc(Document document);
}
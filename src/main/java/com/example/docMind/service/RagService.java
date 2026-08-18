package com.example.docMind.service;

import com.example.docMind.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }
    public String ask(Question question) {

        List<Document> relevantDocs = retrieveDocuments(question.question());

        String context = buildContext(relevantDocs);

        return generateAnswer(question.question(), context);
    }
    public Map<String, Object> askWithSources(String question) {
        List<Document> relevantDocs = retrieveDocuments(question);
        String context = buildContext(relevantDocs);
        String answer = generateAnswer(question, context);

        List<Map<String, Object>> sources = relevantDocs.stream()
                .map(doc -> Map.of(
                        "content", doc.getFormattedContent(),
                        "score", doc.getScore() != null ? doc.getScore() : "",
                        "metadata", doc.getMetadata()
                ))
                .toList();

        return Map.of(
                "question", question,
                "answer", answer,
                "sources", sources
        );
    }
    private List<Document> retrieveDocuments(String question) {
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(4).similarityThreshold(0.3).build()
        );
        return documents;
    }
    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "No relevant documents found.";
        }

        return documents.stream()
                .map(doc -> {
                    // Optionally add metadata like source
                    String source = (String) doc.getMetadata().getOrDefault("source", "Unknown");
                    return "[Source: " + source + "]\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }
    private String generateAnswer(String question, String context) {
        SystemMessage systemMessage = new SystemMessage("""
            You are a helpful assistant that answers questions based ONLY 
            on the provided documents.
            
            RULES:
                1. Use the information in the documents below to answer the question.
                2. If the answer can be determined from the documents (even if the exact wording differs),
                    give a concise answer and cite the source.
                3. If the documents do not contain any relevant information that could help answer
                  the question, say: "I don't have enough information to answer this question."
                4. Do NOT use your own knowledge or make up facts.
                5. Be concise and clear.
                6. NEVER perform arithmetic, calculations, or invent numbers, percentages, or formulas.
                   If a specific value, price, or calculation is required but not explicitly given
                   in the documents, simply state that the exact amount or calculation is not specified.
                7. Never assume a monetary value (e.g., $X, $100). Only mention amounts that appear
                   directly in the documents. If none are present, do not invent one.
                8. If the documents contain multiple, separate policies (e.g., one for general orders and
                   one for subscriptions), ONLY apply the policy that specifically matches the user's situation.
                   Do NOT mix rules from unrelated policies. For a subscription question, use only the
                   subscription policy, not the general cancellation rules.   
            
            DOCUMENTS:
            %s
            """.formatted(context));

        UserMessage userMessage = new UserMessage(question);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(prompt)
                .options(ChatOptions.builder()
                        .temperature(0.0).build());
        return requestSpec.call().content();
    }
}

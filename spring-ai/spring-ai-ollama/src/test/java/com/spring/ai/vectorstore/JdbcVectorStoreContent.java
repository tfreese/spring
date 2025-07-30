// Created: 28.06.2025
package com.spring.ai.vectorstore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;

/**
 * @author Thomas Freese
 */
public class JdbcVectorStoreContent {
    private float[] embedding;
    private String id;
    private Media media;
    private Map<String, Object> metadata;
    private String text;

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final JdbcVectorStoreContent that)) {
            return false;
        }

        return Objects.deepEquals(embedding, that.embedding) && Objects.equals(id, that.id)
                && Objects.equals(media, that.media) && Objects.equals(metadata, that.metadata) && Objects.equals(text, that.text);
    }

    public float[] getEmbedding() {
        return Arrays.copyOf(embedding, embedding.length);
    }

    public String getId() {
        return id;
    }

    public Media getMedia() {
        return media;
    }

    public Map<String, Object> getMetadata() {
        return Map.copyOf(metadata);
    }

    public String getText() {
        return text;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(embedding), id, media, metadata, text);
    }

    public void setEmbedding(final float[] embedding) {
        this.embedding = Arrays.copyOf(embedding, embedding.length);
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setMedia(final Media media) {
        this.media = media;
    }

    public void setMetadata(final Map<String, Object> metadata) {
        this.metadata = Map.copyOf(metadata);
    }

    public void setText(final String text) {
        this.text = text;
    }

    public Document toDocument(final Double score) {
        final Map<String, Object> meta = new HashMap<>(getMetadata());
        meta.put(DocumentMetadata.DISTANCE.value(), 1.0D - score);

        return Document.builder()
                .id(getId())
                .score(score)
                .media(getMedia())
                .metadata(meta)
                .text(getText())
                .build();
    }
}

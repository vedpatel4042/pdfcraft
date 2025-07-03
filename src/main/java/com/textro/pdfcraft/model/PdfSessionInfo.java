// PdfSessionInfo.java - New model for session management
package com.textro.pdfcraft.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PdfSessionInfo {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("pageCount")
    private int pageCount;

    @JsonProperty("currentPage")
    private int currentPage = 1;

    @JsonProperty("zoomLevel")
    private float zoomLevel = 1.0f;

    @JsonProperty("isEncrypted")
    private boolean isEncrypted = false;

    @JsonProperty("documentInfo")
    private DocumentInfo documentInfo = new DocumentInfo();

    @JsonProperty("createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonProperty("lastAccessed")
    private LocalDateTime lastAccessed = LocalDateTime.now();

    @JsonProperty("isModified")
    private boolean isModified = false;

    @JsonProperty("totalEdits")
    private int totalEdits = 0;

    // Inner class for document metadata
    public static class DocumentInfo {

        @JsonProperty("title")
        private String title;

        @JsonProperty("author")
        private String author;

        @JsonProperty("subject")
        private String subject;

        @JsonProperty("creator")
        private String creator;

        @JsonProperty("producer")
        private String producer;

        @JsonProperty("keywords")
        private String keywords;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getProducer() {
            return producer;
        }

        public void setProducer(String producer) {
            this.producer = producer;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }
    }

    // Constructors
    public PdfSessionInfo() {
    }

    public PdfSessionInfo(String sessionId, String fileName, int pageCount) {
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.pageCount = pageCount;
    }

    // Utility methods
    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }

    public void incrementTotalEdits() {
        this.totalEdits++;
        this.isModified = true;
        updateLastAccessed();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("fileName", fileName);
        map.put("pageCount", pageCount);
        map.put("currentPage", currentPage);
        map.put("zoomLevel", zoomLevel);
        map.put("isEncrypted", isEncrypted);
        map.put("title", documentInfo.getTitle());
        map.put("author", documentInfo.getAuthor());
        map.put("subject", documentInfo.getSubject());
        map.put("createdAt", createdAt);
        map.put("lastAccessed", lastAccessed);
        map.put("isModified", isModified);
        map.put("totalEdits", totalEdits);
        return map;
    }

    // Getters and setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        updateLastAccessed();
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
        updateLastAccessed();
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public void setDocumentInfo(DocumentInfo documentInfo) {
        this.documentInfo = documentInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
        if (modified) {
            updateLastAccessed();
        }
    }

    public int getTotalEdits() {
        return totalEdits;
    }

    public void setTotalEdits(int totalEdits) {
        this.totalEdits = totalEdits;
    }

    @Override
    public String toString() {
        return "PdfSessionInfo{"
                + "sessionId='" + sessionId + '\''
                + ", fileName='" + fileName + '\''
                + ", pageCount=" + pageCount
                + ", currentPage=" + currentPage
                + ", zoomLevel=" + zoomLevel
                + ", isModified=" + isModified
                + ", totalEdits=" + totalEdits
                + '}';
    }
}

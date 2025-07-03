// PdfEditRequest.java
package com.textro.pdfcraft.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PdfEditRequest {

    @JsonProperty("id")
    private String id;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("pageNumber")
    private int pageNumber;

    @JsonProperty("text")
    private String text;

    @JsonProperty("x")
    private float x;

    @JsonProperty("y")
    private float y;

    @JsonProperty("fontSize")
    private float fontSize = 12f;

    @JsonProperty("fontFamily")
    private String fontFamily = "helvetica";

    @JsonProperty("color")
    private String color = "#000000";

    @JsonProperty("isDeleted")
    private boolean isDeleted = false;

    @JsonProperty("isEditing")
    private boolean isEditing = false;

    @JsonProperty("lastModified")
    private LocalDateTime lastModified = LocalDateTime.now();

    @JsonProperty("editType")
    private EditType editType = EditType.ADD_TEXT;

    public enum EditType {
        ADD_TEXT,
        EDIT_TEXT,
        DELETE_TEXT,
        MOVE_TEXT
    }

    // Default constructor
    public PdfEditRequest() {
    }

    // Constructor for basic text addition
    public PdfEditRequest(String sessionId, int pageNumber, String text, float x, float y) {
        this.sessionId = sessionId;
        this.pageNumber = pageNumber;
        this.text = text;
        this.x = x;
        this.y = y;
    }

    // Full constructor
    public PdfEditRequest(String sessionId, int pageNumber, String text, float x, float y,
            float fontSize, String fontFamily, String color) {
        this.sessionId = sessionId;
        this.pageNumber = pageNumber;
        this.text = text;
        this.x = x;
        this.y = y;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.color = color;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.lastModified = LocalDateTime.now();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        this.lastModified = LocalDateTime.now();
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        this.lastModified = LocalDateTime.now();
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
        this.lastModified = LocalDateTime.now();
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        this.lastModified = LocalDateTime.now();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        this.lastModified = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        this.lastModified = LocalDateTime.now();
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public EditType getEditType() {
        return editType;
    }

    public void setEditType(EditType editType) {
        this.editType = editType;
    }

    @Override
    public String toString() {
        return "PdfEditRequest{"
                + "id='" + id + '\''
                + ", sessionId='" + sessionId + '\''
                + ", pageNumber=" + pageNumber
                + ", text='" + text + '\''
                + ", x=" + x
                + ", y=" + y
                + ", fontSize=" + fontSize
                + ", fontFamily='" + fontFamily + '\''
                + ", color='" + color + '\''
                + ", isDeleted=" + isDeleted
                + ", editType=" + editType
                + '}';
    }
}

// package com.textro.pdfcraft.model;
// public class PdfEditRequest {
//     private String sessionId;
//     private int pageNumber;
//     private String text;
//     private float x;
//     private float y;
//     private float fontSize;
//     private String id;
//     // Getters and setters
//     public void setId(String id) {
//         this.id = id;
//     }
//     public String getId() {
//         return id;
//     }
//     public String getSessionId() {
//         return sessionId;
//     }
//     public void setSessionId(String sessionId) {
//         this.sessionId = sessionId;
//     }
//     public int getPageNumber() {
//         return pageNumber;
//     }
//     public void setPageNumber(int pageNumber) {
//         this.pageNumber = pageNumber;
//     }
//     public String getText() {
//         return text;
//     }
//     public void setText(String text) {
//         this.text = text;
//     }
//     public float getX() {
//         return x;
//     }
//     public void setX(float x) {
//         this.x = x;
//     }
//     public float getY() {
//         return y;
//     }
//     public void setY(float y) {
//         this.y = y;
//     }
//     public float getFontSize() {
//         return fontSize;
//     }
//     public void setFontSize(float fontSize) {
//         this.fontSize = fontSize;
//     }
// }

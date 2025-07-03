// PdfPageState.java
package com.textro.pdfcraft.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PdfPageState {

    @JsonProperty("pageNumber")
    private int pageNumber;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("edits")
    private List<PdfEditRequest> edits = new ArrayList<>();

    @JsonProperty("existingTextElements")
    private List<ExistingTextElement> existingTextElements = new ArrayList<>();

    @JsonProperty("pageProperties")
    private PageProperties pageProperties = new PageProperties();

    @JsonProperty("lastModified")
    private LocalDateTime lastModified = LocalDateTime.now();

    @JsonProperty("isModified")
    private boolean isModified = false;

    // Inner class for page properties
    public static class PageProperties {

        @JsonProperty("width")
        private float width = 595.27f; // A4 width in points

        @JsonProperty("height")
        private float height = 841.89f; // A4 height in points

        @JsonProperty("rotation")
        private int rotation = 0;

        @JsonProperty("marginTop")
        private float marginTop = 72f; // 1 inch

        @JsonProperty("marginBottom")
        private float marginBottom = 72f;

        @JsonProperty("marginLeft")
        private float marginLeft = 72f;

        @JsonProperty("marginRight")
        private float marginRight = 72f;

        // Getters and setters
        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public int getRotation() {
            return rotation;
        }

        public void setRotation(int rotation) {
            this.rotation = rotation;
        }

        public float getMarginTop() {
            return marginTop;
        }

        public void setMarginTop(float marginTop) {
            this.marginTop = marginTop;
        }

        public float getMarginBottom() {
            return marginBottom;
        }

        public void setMarginBottom(float marginBottom) {
            this.marginBottom = marginBottom;
        }

        public float getMarginLeft() {
            return marginLeft;
        }

        public void setMarginLeft(float marginLeft) {
            this.marginLeft = marginLeft;
        }

        public float getMarginRight() {
            return marginRight;
        }

        public void setMarginRight(float marginRight) {
            this.marginRight = marginRight;
        }
    }

    // Inner class for existing text elements
    public static class ExistingTextElement {

        @JsonProperty("id")
        private String id;

        @JsonProperty("pageNumber")
        private int pageNumber;

        @JsonProperty("text")
        private String text;

        @JsonProperty("x")
        private float x;

        @JsonProperty("y")
        private float y;

        @JsonProperty("width")
        private float width;

        @JsonProperty("height")
        private float height;

        @JsonProperty("fontSize")
        private float fontSize;

        @JsonProperty("isSelected")
        private boolean isSelected = false;

        @JsonProperty("isEditable")
        private boolean isEditable = true;

        public ExistingTextElement() {
        }

        public ExistingTextElement(String id, int pageNumber, String text, float x, float y,
                float width, float height, float fontSize) {
            this.id = id;
            this.pageNumber = pageNumber;
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.fontSize = fontSize;
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public float getFontSize() {
            return fontSize;
        }

        public void setFontSize(float fontSize) {
            this.fontSize = fontSize;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public boolean isEditable() {
            return isEditable;
        }

        public void setEditable(boolean editable) {
            isEditable = editable;
        }
    }

    // Constructors
    public PdfPageState() {
    }

    public PdfPageState(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public PdfPageState(int pageNumber, int totalPages) {
        this.pageNumber = pageNumber;
        this.totalPages = totalPages;
    }

    // Methods for managing edits
    public void addEdit(PdfEditRequest edit) {
        edit.setPageNumber(this.pageNumber);
        this.edits.add(edit);
        this.isModified = true;
        this.lastModified = LocalDateTime.now();
    }

    public boolean removeEdit(String editId) {
        boolean removed = this.edits.removeIf(edit -> editId.equals(edit.getId()));
        if (removed) {
            this.isModified = true;
            this.lastModified = LocalDateTime.now();
        }
        return removed;
    }

    public PdfEditRequest getEdit(String editId) {
        return this.edits.stream()
                .filter(edit -> editId.equals(edit.getId()))
                .findFirst()
                .orElse(null);
    }

    public List<PdfEditRequest> getActiveEdits() {
        return this.edits.stream()
                .filter(edit -> !edit.isDeleted())
                .toList();
    }

    public void clearEdits() {
        this.edits.clear();
        this.isModified = true;
        this.lastModified = LocalDateTime.now();
    }

    // Methods for managing existing text elements
    public void addExistingTextElement(ExistingTextElement element) {
        element.setPageNumber(this.pageNumber);
        this.existingTextElements.add(element);
    }

    public void clearExistingTextElements() {
        this.existingTextElements.clear();
    }

    public List<ExistingTextElement> getSelectableTextElements() {
        return this.existingTextElements.stream()
                .filter(ExistingTextElement::isEditable)
                .toList();
    }

    // Getters and setters
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<PdfEditRequest> getEdits() {
        return edits;
    }

    public void setEdits(List<PdfEditRequest> edits) {
        this.edits = edits;
        this.isModified = true;
        this.lastModified = LocalDateTime.now();
    }

    public List<ExistingTextElement> getExistingTextElements() {
        return existingTextElements;
    }

    public void setExistingTextElements(List<ExistingTextElement> existingTextElements) {
        this.existingTextElements = existingTextElements;
    }

    public PageProperties getPageProperties() {
        return pageProperties;
    }

    public void setPageProperties(PageProperties pageProperties) {
        this.pageProperties = pageProperties;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
        if (modified) {
            this.lastModified = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "PdfPageState{"
                + "pageNumber=" + pageNumber
                + ", totalPages=" + totalPages
                + ", editsCount=" + edits.size()
                + ", existingTextElementsCount=" + existingTextElements.size()
                + ", isModified=" + isModified
                + ", lastModified=" + lastModified
                + '}';
    }
}

// package com.textro.pdfcraft.model;
// import java.util.ArrayList;
// import java.util.List;
// public class PdfPageState {
//     private int pageNumber;
//     private int totalPages;
//     private List<PdfEditRequest> edits = new ArrayList<>();
//     public PdfPageState() {
//     }
//     public PdfPageState(int pageNumber) {
//         this.pageNumber = pageNumber;
//     }
//     // Getters and setters
//     public int getPageNumber() {
//         return pageNumber;
//     }
//     public void setPageNumber(int pageNumber) {
//         this.pageNumber = pageNumber;
//     }
//     public int getTotalPages() {
//         return totalPages;
//     }
//     public void setTotalPages(int totalPages) {
//         this.totalPages = totalPages;
//     }
//     public List<PdfEditRequest> getEdits() {
//         return edits;
//     }
//     public void setEdits(List<PdfEditRequest> edits) {
//         this.edits = edits;
//     }
// }

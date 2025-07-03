// TextEditRequest.java - Updated to work with PdfEditRequest
package com.textro.pdfcraft.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextEditRequest {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("pageNumber")
    private int pageNumber;

    @JsonProperty("textId")
    private String textId;

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

    @JsonProperty("operation")
    private TextOperation operation = TextOperation.ADD;

    public enum TextOperation {
        ADD,
        EDIT,
        DELETE,
        MOVE
    }

    // Default constructor
    public TextEditRequest() {
    }

    // Constructor for converting from PdfEditRequest
    public TextEditRequest(PdfEditRequest pdfEditRequest) {
        this.sessionId = pdfEditRequest.getSessionId();
        this.pageNumber = pdfEditRequest.getPageNumber();
        this.textId = pdfEditRequest.getId();
        this.text = pdfEditRequest.getText();
        this.x = pdfEditRequest.getX();
        this.y = pdfEditRequest.getY();
        this.fontSize = pdfEditRequest.getFontSize();
        this.fontFamily = pdfEditRequest.getFontFamily();
        this.color = pdfEditRequest.getColor();

        // Map edit type to operation
        switch (pdfEditRequest.getEditType()) {
            case ADD_TEXT ->
                this.operation = TextOperation.ADD;
            case EDIT_TEXT ->
                this.operation = TextOperation.EDIT;
            case DELETE_TEXT ->
                this.operation = TextOperation.DELETE;
            case MOVE_TEXT ->
                this.operation = TextOperation.MOVE;
        }
    }

    // Full constructor
    public TextEditRequest(String sessionId, int pageNumber, String textId, String text,
            float x, float y, float fontSize, String fontFamily, String color) {
        this.sessionId = sessionId;
        this.pageNumber = pageNumber;
        this.textId = textId;
        this.text = text;
        this.x = x;
        this.y = y;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.color = color;
    }

    // Method to convert to PdfEditRequest
    public PdfEditRequest toPdfEditRequest() {
        PdfEditRequest pdfEditRequest = new PdfEditRequest();
        pdfEditRequest.setId(this.textId);
        pdfEditRequest.setSessionId(this.sessionId);
        pdfEditRequest.setPageNumber(this.pageNumber);
        pdfEditRequest.setText(this.text);
        pdfEditRequest.setX(this.x);
        pdfEditRequest.setY(this.y);
        pdfEditRequest.setFontSize(this.fontSize);
        pdfEditRequest.setFontFamily(this.fontFamily);
        pdfEditRequest.setColor(this.color);

        // Map operation to edit type
        switch (this.operation) {
            case ADD ->
                pdfEditRequest.setEditType(PdfEditRequest.EditType.ADD_TEXT);
            case EDIT ->
                pdfEditRequest.setEditType(PdfEditRequest.EditType.EDIT_TEXT);
            case DELETE ->
                pdfEditRequest.setEditType(PdfEditRequest.EditType.DELETE_TEXT);
            case MOVE ->
                pdfEditRequest.setEditType(PdfEditRequest.EditType.MOVE_TEXT);
        }

        return pdfEditRequest;
    }

    // Getters and Setters
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

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
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

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public TextOperation getOperation() {
        return operation;
    }

    public void setOperation(TextOperation operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "TextEditRequest{"
                + "sessionId='" + sessionId + '\''
                + ", pageNumber=" + pageNumber
                + ", textId='" + textId + '\''
                + ", text='" + text + '\''
                + ", x=" + x
                + ", y=" + y
                + ", fontSize=" + fontSize
                + ", fontFamily='" + fontFamily + '\''
                + ", color='" + color + '\''
                + ", operation=" + operation
                + '}';
    }
}

// package com.textro.pdfcraft.model;
// import com.fasterxml.jackson.annotation.JsonProperty;
// public class TextEditRequest {
//     @JsonProperty("sessionId")
//     private String sessionId;
//     @JsonProperty("pageNumber")
//     private int pageNumber;
//     @JsonProperty("textId")
//     private String textId;
//     @JsonProperty("text")
//     private String text;
//     @JsonProperty("x")
//     private float x;
//     @JsonProperty("y")
//     private float y;
//     @JsonProperty("fontSize")
//     private float fontSize = 12f;
//     @JsonProperty("fontFamily")
//     private String fontFamily = "Helvetica";
//     @JsonProperty("color")
//     private String color = "#000000";
//     // Default constructor
//     public TextEditRequest() {
//     }
//     // Full constructor
//     public TextEditRequest(String sessionId, int pageNumber, String textId, String text,
//             float x, float y, float fontSize, String fontFamily, String color) {
//         this.sessionId = sessionId;
//         this.pageNumber = pageNumber;
//         this.textId = textId;
//         this.text = text;
//         this.x = x;
//         this.y = y;
//         this.fontSize = fontSize;
//         this.fontFamily = fontFamily;
//         this.color = color;
//     }
//     // Getters and Setters
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
//     public String getTextId() {
//         return textId;
//     }
//     public void setTextId(String textId) {
//         this.textId = textId;
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
//     public String getFontFamily() {
//         return fontFamily;
//     }
//     public void setFontFamily(String fontFamily) {
//         this.fontFamily = fontFamily;
//     }
//     public String getColor() {
//         return color;
//     }
//     public void setColor(String color) {
//         this.color = color;
//     }
//     @Override
//     public String toString() {
//         return "TextEditRequest{"
//                 + "sessionId='" + sessionId + '\''
//                 + ", pageNumber=" + pageNumber
//                 + ", textId='" + textId + '\''
//                 + ", text='" + text + '\''
//                 + ", x=" + x
//                 + ", y=" + y
//                 + ", fontSize=" + fontSize
//                 + ", fontFamily='" + fontFamily + '\''
//                 + ", color='" + color + '\''
//                 + '}';
//     }
// }

package com.sjmt.SJMT.DTO.RequestDTO;

/**
 * Payload sent by the React frontend when an unhandled error occurs
 * (ErrorBoundary, window.onerror, or unhandledrejection).
 */
public class ClientErrorRequest {

    /** Short error message (e.g. "Cannot read properties of null") */
    private String message;

    /** Full JavaScript stack trace */
    private String stack;

    /** Browser URL where the error occurred */
    private String url;

    /** Source of the error: "ErrorBoundary", "window.onerror", or "unhandledrejection" */
    private String source;

    /** ISO timestamp from the browser */
    private String timestamp;

    /** Optional: additional context (component name, user action, etc.) */
    private String context;

    // ── Getters & Setters ─────────────────────────────────────────

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStack() { return stack; }
    public void setStack(String stack) { this.stack = stack; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}

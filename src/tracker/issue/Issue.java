package tracker.issue;

public interface Issue extends ReadableIssue {
    void setTitle(String title);

    void setDescription(String description);

    void setStatus(Status status);
}

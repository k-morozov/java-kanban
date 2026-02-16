package tracker.issue;

public interface ReadableIssue {
    int getId();

    String getTitle();

    String getDescription();

    Status getStatus();
}

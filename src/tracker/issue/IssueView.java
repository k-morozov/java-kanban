package tracker.issue;

import java.util.Objects;

public abstract class IssueView<T extends Issue> implements ReadableIssue, Serializer {
    private final T issue;

    public IssueView(T issue) {
        this.issue = Objects.requireNonNull(issue);
    }

    T getIssue() {
        return issue;
    }

    @Override
    public int getId() {
        return issue.getId();
    }

    @Override
    public String getTitle() {
        return issue.getTitle();
    }

    @Override
    public String getDescription() {
        return issue.getDescription();
    }

    @Override
    public Status getStatus() {
        return issue.getStatus();
    }

    @Override
    public int hashCode() {
        return issue.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof IssueView<?> other) {
            return this.issue.equals(other.issue);
        }

        return false;
    }
}

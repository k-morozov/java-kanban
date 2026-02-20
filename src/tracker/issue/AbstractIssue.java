package tracker.issue;

public abstract class AbstractIssue implements Issue {
    private final int issueId;
    private String title;
    private String description;
    private Status status;

    public AbstractIssue(String title, String description, int issueId, Status status) {
        this.issueId = issueId;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    @Override
    public int getId() {
        return issueId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Issue task = (Issue) obj;
        return this.getId() == task.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.getId());
    }
}

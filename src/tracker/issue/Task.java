package tracker.issue;

public class Task extends AbstractIssue {
    public Task(String title, String description, int issueId, Status status) {
        super(title, description, issueId, status);
    }
}
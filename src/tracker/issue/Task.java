package tracker.issue;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task extends AbstractIssue {
    public Task(String title, String description, int issueId, Status status) {
        super(title, description, issueId, status);
    }

    public Task(String title, String description, int issueId, Status status, LocalDateTime startTime, Duration duration) {
        super(title, description, issueId, status, startTime, duration);
    }
}
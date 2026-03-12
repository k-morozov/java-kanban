package tracker.issue;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends AbstractIssue implements ReadableSubtask {
    private int epicId;

    public Subtask(String title, String description, int issueId, Status status, int epicId) {
        super(title, description, issueId, status);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, int issueId, Status status, LocalDateTime startTime, Duration duration, int epicId) {
        super(title, description, issueId, status, startTime, duration);
        this.epicId = epicId;
    }

    @Override
    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }
}

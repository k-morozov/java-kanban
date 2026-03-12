package tracker.issue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public abstract class AbstractIssue implements Issue {
    private final int issueId;
    private String title;
    private String description;
    private Status status;
    protected Optional<LocalDateTime> startTime;
    protected Optional<Duration> duration;

    public AbstractIssue(String title, String description, int issueId, Status status) {
        this.issueId = issueId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = Optional.empty();
        this.duration = Optional.empty();
    }

    public AbstractIssue(String title, String description, int issueId, Status status, LocalDateTime startTime, Duration duration) {
        this.issueId = issueId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = Optional.of(startTime);
        this.duration = Optional.of(duration);
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
    public Optional<LocalDateTime> getStartTime() {
        return startTime;
    }

    @Override
    public Optional<Duration> getDuration() {
        return duration;
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        if (startTime.isEmpty() || duration.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(startTime.get().plus(duration.get()));
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

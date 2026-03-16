package tracker.issue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Epic extends AbstractIssue implements ReadableEpic {
    private final ArrayList<Integer> subtaskIds;
    private Optional<LocalDateTime> endTime;

    public Epic(String title, String description, int issueId) {
        super(title, description, issueId, Status.NEW);
        this.subtaskIds = new ArrayList<>();
        this.endTime = Optional.empty();
    }

    @Override
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    public void updateTime(LocalDateTime start, Duration duration, LocalDateTime end) {
        this.startTime = Optional.of(start);
        this.endTime = Optional.of(end);
        this.duration = Optional.of(duration);
    }

    public void resetTime() {
        this.startTime = Optional.empty();
        this.endTime = Optional.empty();
        this.duration = Optional.empty();
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return endTime;
    }
}
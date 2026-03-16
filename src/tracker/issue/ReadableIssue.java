package tracker.issue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ReadableIssue {
    int getId();

    String getTitle();

    String getDescription();

    Status getStatus();

    Optional<LocalDateTime> getStartTime();

    Optional<Duration> getDuration();

    Optional<LocalDateTime> getEndTime();
}

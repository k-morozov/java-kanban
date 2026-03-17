package tracker.Server.Input;

import tracker.issue.Status;

public class SubtaskCreateInput {
    public String title;
    public String description;
    public Status status;
    public String startTime;
    public String duration;
    public int epicId;

    @Override
    public String toString() {
        return "SubtaskCreateInput{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", startTime='" + startTime + '\'' +
                ", duration='" + duration + '\'' +
                ", epicId=" + epicId +
                '}';
    }

    public void validate() {
        if (title == null || title.isEmpty()) {
            throw new InputValidationException("Title must be non empty");
        }

        if (status == null) {
            throw new InputValidationException("Status must be non empty");
        }

        if (epicId <= 0) {
            throw new InputValidationException("EpicId must be positive");
        }
    }
}

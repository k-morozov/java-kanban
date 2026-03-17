package tracker.Server.Input;

import tracker.issue.Status;

public class TaskCreateInput {
    public String title;
    public String description;
    public Status status;
    public String startTime;
    public String duration;

    @Override
    public String toString() {
        return "TaskCreateInput{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", startTime='" + startTime + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    public void validate() {
        // simple validation
        if (title == null || title.isEmpty()) {
            throw new InputValidationException("Title must be non empty");
        }

        if (status == null) {
            throw new InputValidationException("Status must be non empty");
        }
    }
}

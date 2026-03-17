package tracker.Server.Output;

import tracker.issue.Status;
import tracker.issue.TaskView;

public class TaskGetOutput {
    public String title;
    public String description;
    public Status status;
    public String startTime;
    public String duration;

    @Override
    public String toString() {
        return "TaskGetOutput{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", startTime='" + startTime + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    public static TaskGetOutput from(TaskView view) {
        TaskGetOutput output = new TaskGetOutput();
        output.title = view.getTitle();
        output.description = view.getDescription();
        output.status = view.getStatus();

        if (view.getStartTime().isPresent()) {
            output.startTime = String.valueOf(view.getStartTime().orElseThrow());
        }

        if (view.getDuration().isPresent()) {
            output.duration = String.valueOf(view.getDuration().orElseThrow());
        }

        return output;
    }
}

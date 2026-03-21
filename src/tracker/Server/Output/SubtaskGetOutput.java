package tracker.Server.Output;

import tracker.issue.Status;
import tracker.issue.SubtaskView;

public class SubtaskGetOutput {
    public String title;
    public String description;
    public Status status;
    public String startTime;
    public String duration;
    public int epicId;

    @Override
    public String toString() {
        return "SubtaskGetOutput{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", startTime='" + startTime + '\'' +
                ", duration='" + duration + '\'' +
                ", epicId=" + epicId +
                '}';
    }

    public static SubtaskGetOutput from(SubtaskView view) {
        SubtaskGetOutput output = new SubtaskGetOutput();
        output.title = view.getTitle();
        output.description = view.getDescription();
        output.status = view.getStatus();
        output.epicId = view.getEpicId();

        if (view.getStartTime().isPresent()) {
            output.startTime = String.valueOf(view.getStartTime().orElseThrow());
        }

        if (view.getDuration().isPresent()) {
            output.duration = String.valueOf(view.getDuration().orElseThrow());
        }

        return output;
    }
}

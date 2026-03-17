package tracker.Server.Output;

import tracker.issue.ReadableIssue;
import tracker.issue.Status;

public class HistoryGetOutput {
    public int id;
    public String title;
    public String description;
    public Status status;
    public String startTime;
    public String duration;
    public String endTime;

    public static HistoryGetOutput from(ReadableIssue issue) {
        HistoryGetOutput output = new HistoryGetOutput();
        output.id = issue.getId();
        output.title = issue.getTitle();
        output.description = issue.getDescription();
        output.status = issue.getStatus();

        issue.getStartTime().ifPresent(t -> output.startTime = t.toString());
        issue.getDuration().ifPresent(d -> output.duration = d.toString());
        issue.getEndTime().ifPresent(t -> output.endTime = t.toString());

        return output;
    }
}

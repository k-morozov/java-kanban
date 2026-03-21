package tracker.Server.Output;

import tracker.issue.EpicView;
import tracker.issue.Status;

public class EpicGetOutput {
    public String title;
    public String description;
    public Status status;
    public String startTime;
    public String duration;
    public String endTime;

    public static EpicGetOutput from(EpicView view) {
        EpicGetOutput output = new EpicGetOutput();
        output.title = view.getTitle();
        output.description = view.getDescription();
        output.status = view.getStatus();

        view.getStartTime().ifPresent(t -> output.startTime = t.toString());
        view.getDuration().ifPresent(d -> output.duration = d.toString());
        view.getEndTime().ifPresent(t -> output.endTime = t.toString());

        return output;
    }
}

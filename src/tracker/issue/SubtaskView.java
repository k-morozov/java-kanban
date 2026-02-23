package tracker.issue;

public final class SubtaskView extends IssueView<Subtask> implements ReadableSubtask {
    public SubtaskView(Subtask subtask) {
        super(subtask);
    }

    @Override
    public int getEpicId() {
        return getIssue().getEpicId();
    }

    public String serialize(Formatter formatter) {
        return formatter.serialize(getIssue());
    }
}

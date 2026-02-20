package tracker.issue;

public final class SubtaskView extends IssueView<Subtask> implements ReadableSubtask {
    public SubtaskView(Subtask subtask) {
        super(subtask);
    }

    @Override
    public int getEpicId() {
        return getIssue().getEpicId();
    }
}

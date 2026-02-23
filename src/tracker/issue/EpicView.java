package tracker.issue;

import java.util.List;

public final class EpicView extends IssueView<Epic> implements ReadableEpic {
    public EpicView(Epic epic) {
        super(epic);
    }

    @Override
    public List<Integer> getSubtaskIds() {
        return List.copyOf(getIssue().getSubtaskIds());
    }

    @Override
    public String serialize(Formatter formatter) {
        return formatter.serialize(getIssue());
    }
}

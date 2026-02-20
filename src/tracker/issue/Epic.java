package tracker.issue;

import java.util.ArrayList;
import java.util.List;

public final class Epic extends AbstractIssue implements ReadableEpic {
    private final ArrayList<Integer> subtaskIds;

    public Epic(String title, String description, int issueId) {
        super(title, description, issueId, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    @Override
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }
}
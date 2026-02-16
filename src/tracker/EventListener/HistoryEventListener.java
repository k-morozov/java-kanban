package tracker.EventListener;

import tracker.HistoryManager.HistoryManager;
import tracker.issue.Issue;

public class HistoryEventListener implements EventListener {
    private final HistoryManager manager;

    public HistoryEventListener(HistoryManager manager) {
        this.manager = manager;
    }

    @Override
    public void onAccessed(Issue issue) {
        manager.add(issue);
    }

    @Override
    public void onDelete(int id) {
        manager.remove(id);
    }
}

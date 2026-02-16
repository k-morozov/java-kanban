package tracker.EventListener;

import tracker.issue.Issue;

public interface EventListener {
    void onAccessed(Issue issue);

    void onDelete(int id);
}

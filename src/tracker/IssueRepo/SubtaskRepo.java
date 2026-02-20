package tracker.IssueRepo;

import tracker.EventListener.EventListener;
import tracker.issue.Subtask;

public final class SubtaskRepo extends AbstractIssueRepo<Subtask> {
    public SubtaskRepo(EventListener listener, Policy<Subtask> policy) {
        super(listener, policy);
    }
}
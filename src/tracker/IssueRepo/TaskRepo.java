package tracker.IssueRepo;

import tracker.EventListener.EventListener;
import tracker.issue.Task;

public final class TaskRepo extends AbstractIssueRepo<Task> {
    public TaskRepo(EventListener listener, Policy<Task> policy) {
        super(listener, policy);
    }
}

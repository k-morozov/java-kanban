package tracker.IssueRepo;

import tracker.EventListener.EventListener;
import tracker.issue.Epic;

public final class EpicRepo extends AbstractIssueRepo<Epic> {
    public EpicRepo(EventListener listener, Policy<Epic> policy) {
        super(listener, policy);
    }
}

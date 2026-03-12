package tracker.TaskManager;

import tracker.IssueRepo.*;

public class InMemoryTaskManager extends AbstractTaskManager {
    public InMemoryTaskManager() {
        super(InMemoryPrioritizedPolicy::create, InMemoryPolicy::create, InMemoryPolicy::create);
    }

}
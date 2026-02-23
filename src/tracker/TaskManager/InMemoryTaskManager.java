package tracker.TaskManager;

import tracker.IssueRepo.*;

public class InMemoryTaskManager extends AbstractTaskManager {
    public InMemoryTaskManager() {
        super(InMemoryPolicy::create);
    }

}
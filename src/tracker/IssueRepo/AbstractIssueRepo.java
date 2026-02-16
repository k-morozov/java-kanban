package tracker.IssueRepo;

import tracker.EventListener.EventListener;
import tracker.issue.Issue;

import java.util.List;
import java.util.Objects;

public abstract class AbstractIssueRepo<T extends Issue> implements IssueRepo<T> {
    private final EventListener listener;
    private final Policy<T> policy;

    public AbstractIssueRepo(EventListener listener, Policy<T> policy) {
        this.listener = Objects.requireNonNull(listener);
        this.policy = Objects.requireNonNull(policy);
    }

    @Override
    public boolean save(T issue) {
        return policy.save(issue);
    }

    @Override
    public void update(T issue) {
        policy.update(issue);
    }

    @Override
    public T deleteById(int id) {
        listener.onDelete(id);
        return policy.deleteById(id);
    }

    @Override
    public void deleteAll() {
        for (Issue issue : findAll()) {
            listener.onDelete(issue.getId());
        }
        policy.deleteAll();
    }

    @Override
    public T findById(int id) {
        T issue = policy.findById(id);
        if (issue != null) {
            listener.onAccessed(issue);
        }
        return issue;
    }

    @Override
    public List<T> findAll() {
        return policy.findAll();
    }
}
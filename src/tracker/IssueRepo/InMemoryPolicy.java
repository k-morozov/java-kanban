package tracker.IssueRepo;

import tracker.issue.Issue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class InMemoryPolicy<T extends Issue> implements Policy<T> {
    private final HashMap<Integer, T> issues;

    public static <T extends Issue> InMemoryPolicy<T> create() {
        return new InMemoryPolicy<>();
    }

    private InMemoryPolicy() {
        this.issues = new HashMap<>();
    }

    @Override
    public boolean save(T issue) {
        issues.put(issue.getId(), issue);
        return true;
    }

    @Override
    public void update(T issue) {
        if (!issues.containsKey(issue.getId())) {
            throw new IllegalStateException("Issue does not exist for updating");
        }
        issues.put(issue.getId(), issue);
    }

    @Override
    public T findById(int id) {
        return issues.get(id);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(issues.values());
    }

    @Override
    public T deleteById(int id) {
        return issues.remove(id);
    }

    @Override
    public void deleteAll() {
        issues.clear();
    }
}

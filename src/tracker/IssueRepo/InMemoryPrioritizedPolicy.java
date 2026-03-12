package tracker.IssueRepo;

import tracker.issue.Issue;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public final class InMemoryPrioritizedPolicy<T extends Issue> implements Policy<T> {
    private final HashMap<Integer, T> issues;
    private final TreeSet<T> prioritizedIssues;

    public static <T extends Issue> InMemoryPrioritizedPolicy<T> create() {
        return new InMemoryPrioritizedPolicy<>();
    }

    private InMemoryPrioritizedPolicy() {
        this.issues = new HashMap<>();
        this.prioritizedIssues = new TreeSet<>((lhs, rhs) -> {
            // @todo
            if  (lhs.getStartTime().get().isBefore(rhs.getStartTime().get())) {
                return -1;
            }

            if  (lhs.getStartTime().get().isAfter(rhs.getStartTime().get())) {
                return 1;
            }
            return 0;
        });

    }

    @Override
    public boolean save(T issue) {
        issues.put(issue.getId(), issue);
        if (issue.getStartTime().isPresent()) {
            prioritizedIssues.add(issue);
        }
        return true;
    }

    @Override
    public void update(T issue) {
        if (!issues.containsKey(issue.getId())) {
            throw new IllegalStateException("Issue does not exist for updating");
        }
        Issue old = issues.put(issue.getId(), issue);
        if (old != null && old.getStartTime().isPresent()) {
            prioritizedIssues.remove(old);
        }

        if (issue.getStartTime().isPresent()) {
            prioritizedIssues.add(issue);
        }
    }

    @Override
    public T findById(int id) {
        return issues.get(id);
    }

    @Override
    public List<T> findAll() {
        return prioritizedIssues.stream().toList();
    }

    @Override
    public T deleteById(int id) {
        T removed = issues.remove(id);
        if (removed != null && removed.getStartTime().isPresent()) {
            prioritizedIssues.remove(removed);
        }
        return removed;
    }

    @Override
    public void deleteAll() {
        issues.clear();
        prioritizedIssues.clear();
    }
}

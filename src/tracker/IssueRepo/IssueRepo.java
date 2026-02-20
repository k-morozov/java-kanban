package tracker.IssueRepo;

import tracker.issue.Issue;

public interface IssueRepo<T extends Issue> extends ReadableIssueRepo<T> {
    boolean save(T issue);

    void update(T issue);

    T deleteById(int id);

    void deleteAll();
}

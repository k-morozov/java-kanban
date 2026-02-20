package tracker.IssueRepo;

import tracker.issue.Issue;

import java.util.List;

public interface ReadableIssueRepo<T extends Issue> {
    T findById(int id);

    List<T> findAll();
}

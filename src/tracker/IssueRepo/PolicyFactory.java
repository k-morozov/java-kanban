package tracker.IssueRepo;

import tracker.issue.Issue;

@FunctionalInterface
public interface PolicyFactory {
    <I extends Issue> Policy<I> create();
}

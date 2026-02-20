package tracker.IssueRepo;

import tracker.issue.Issue;

public interface Policy<T extends Issue> extends IssueRepo<T> {
}

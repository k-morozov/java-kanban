package tracker.IssueRepo;

import tracker.issue.Issue;

public interface Register {
    <T extends Issue> void register(Class<T> cls, IssueRepo<T> repo);

    <T extends Issue> IssueRepo<T> getRepo(Class<T> cls);
}

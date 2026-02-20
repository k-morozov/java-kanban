package tracker.IssueRepo;

import tracker.issue.Issue;

import java.util.HashMap;
import java.util.Map;

public final class RepoRegister implements Register {
    private final Map<Class<? extends Issue>, IssueRepo<?>> repos = new HashMap<>();

    @Override
    public <T extends Issue> void register(Class<T> cls, IssueRepo<T> repo) {
        repos.put(cls, repo);
    }

    @Override
    public <T extends Issue> IssueRepo<T> getRepo(Class<T> cls) {
        IssueRepo<T> repo = (IssueRepo<T>)repos.get(cls);
        if (repo == null) {
            throw new IllegalArgumentException("Need to register repo " + cls.getName());
        }
        return repo;
    }
}

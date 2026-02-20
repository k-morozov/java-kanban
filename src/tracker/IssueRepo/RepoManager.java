package tracker.IssueRepo;

import tracker.issue.*;

import java.util.List;

public final class RepoManager {
    private final Register register;

    public RepoManager(Register register) {
        this.register = register;
    }

    public <T extends Issue> T get(Class<T> cls, int id) {
        return register.getRepo(cls).findById(id);
    }

    public <T extends Issue> List<T> getAll(Class<T> cls) {
        return register.getRepo(cls).findAll();
    }

    public <T extends Issue> void save(Class<T> cls, T issue) {
        register.getRepo(cls).save(issue);
    }

    public <T extends Issue> void update(Class<T> cls, T issue) {
        register.getRepo(cls).update(issue);
    }

    public <T extends Issue> T remove(Class<T> cls, int id) {
        return register.getRepo(cls).deleteById(id);
    }

    public <T extends Issue> void removeAll(Class<T> cls) {
        register.getRepo(cls).deleteAll();
    }
}

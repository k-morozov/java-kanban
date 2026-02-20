package tracker.HistoryManager;

import tracker.issue.ReadableIssue;

import java.util.List;

public interface HistoryManager {

    void add(ReadableIssue task);

    void remove(int id);

    List<ReadableIssue> getHistory();
}
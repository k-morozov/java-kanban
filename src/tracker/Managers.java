package tracker;

import tracker.HistoryManager.HistoryManager;
import tracker.HistoryManager.InMemoryHistoryManager;
import tracker.TaskManager.InMemoryTaskManager;
import tracker.TaskManager.TaskManager;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
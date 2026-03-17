package tracker;

import org.junit.jupiter.api.Test;
import tracker.HistoryManager.HistoryManager;
import tracker.HistoryManager.InMemoryHistoryManager;
import tracker.TaskManager.ConflictIssueTimeException;
import tracker.TaskManager.InMemoryTaskManager;
import tracker.TaskManager.NotFoundException;
import tracker.TaskManager.TaskManager;
import tracker.issue.TaskView;
import tracker.issue.Status;
import tracker.issue.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    private final LocalDateTime defaultStartTime = LocalDateTime.parse("2007-12-03T10:15:30.");
    private final Duration defaultDuration = Duration.ofSeconds(10, 0);

    @Test
    void shouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager);
        assertInstanceOf(TaskManager.class, manager);

        TaskView task = manager.createTask("Тестовая задача", "Описание", Status.NEW, defaultStartTime, defaultDuration);
        assertNotNull(task);
        assertNotNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldThrowConflict() {
        TaskManager manager = Managers.getDefault();

        TaskView task1 = manager.createTask("Тестовая задача", "Описание", Status.NEW, defaultStartTime, defaultDuration);
        assertThrows(ConflictIssueTimeException.class, ()-> manager.createTask("Тестовая задача", "Описание", Status.NEW,
            defaultStartTime, defaultDuration));
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager);
        assertInstanceOf(HistoryManager.class, historyManager);

        Task task = new Task("Задача", "Описание", 1, Status.NEW, defaultStartTime, defaultDuration);
        historyManager.add(task);

        assertNotNull(historyManager.getHistory());
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void shouldReturnNewInstanceEachTime() {
        TaskManager manager1 = Managers.getDefault();
        TaskManager manager2 = Managers.getDefault();

        assertNotSame(manager1, manager2);

        TaskView task1 = manager1.createTask("Задача 1", "Описание", Status.NEW, defaultStartTime, defaultDuration);

        assertNotNull(manager1.getTask(task1.getId()));
        assertThrows(NotFoundException.class, () -> manager2.getTask(task1.getId()));
    }

    @Test
    void shouldReturnNewHistoryManagerInstanceEachTime() {
        HistoryManager history1 = Managers.getDefaultHistory();
        HistoryManager history2 = Managers.getDefaultHistory();

        assertNotSame(history1, history2);

        Task task = new Task("Задача", "Описание", 1, Status.NEW, defaultStartTime, defaultDuration);
        history1.add(task);

        assertEquals(1, history1.getHistory().size());
        assertEquals(0, history2.getHistory().size());
    }

    @Test
    void shouldReturnInMemoryTaskManager() {
        TaskManager manager = Managers.getDefault();

        assertInstanceOf(InMemoryTaskManager.class, manager);
    }

    @Test
    void shouldReturnInMemoryHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertInstanceOf(InMemoryHistoryManager.class, historyManager);
    }
}

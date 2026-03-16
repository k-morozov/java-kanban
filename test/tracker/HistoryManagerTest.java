package tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.HistoryManager.HistoryManager;
import tracker.issue.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    private final static LocalDateTime defaultStartTime = LocalDateTime.parse("2007-12-03T10:15:30.");
    private final static Duration defaultDuration = Duration.ofSeconds(10, 0);

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task("Task 1", "Description 1", 1, Status.NEW, defaultStartTime, defaultDuration);
        task2 = new Task("Task 2", "Description 2", 2, Status.NEW, defaultStartTime, defaultDuration);
        task3 = new Task("Task 3", "Description 3", 3, Status.NEW, defaultStartTime, defaultDuration);
    }

    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task1);
        List<ReadableIssue> history = historyManager.getHistory();
        assertNotNull(history, "History should not be null.");
        assertEquals(1, history.size(), "History size should be 1.");
        assertEquals(task1, history.get(0), "History should contain the added task.");
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<ReadableIssue> history = historyManager.getHistory();
        assertNotNull(history, "History list should be initialized even if empty.");
        assertTrue(history.isEmpty(), "History should be empty by default.");
    }

    @Test
    void shouldNotAddNullToHistory() {
        historyManager.add(null);
        List<ReadableIssue> history = historyManager.getHistory();
        assertEquals(0, history.size(), "History size should remain 0 after adding null.");
    }

    @Test
    void shouldRemoveOldDuplicateAndMoveTaskToTail() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<ReadableIssue> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Duplicates should be removed from history.");
        assertEquals(task2, history.get(0), "The first element should be task2.");
        assertEquals(task1, history.get(1), "The re-added task should be moved to the end (tail).");
    }

    @Test
    void shouldRemoveFromBeginningOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());
        List<ReadableIssue> history = historyManager.getHistory();

        assertEquals(2, history.size(), "History size should be 2.");
        assertEquals(task2, history.get(0), "Task 2 should now be at the head.");
    }

    @Test
    void shouldRemoveFromMiddleOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());
        List<ReadableIssue> history = historyManager.getHistory();

        assertEquals(2, history.size(), "History size should be 2.");
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveFromEndOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());
        List<ReadableIssue> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2, history.get(1), "Task 2 should now be the tail.");
    }

    @Test
    void shouldHandleRemovingOnlyElement() {
        historyManager.add(task1);
        historyManager.remove(task1.getId());

        List<ReadableIssue> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "History should be empty after removing the only element.");
    }

    @Test
    void shouldReturnCopyOfHistory() {
        historyManager.add(task1);
        List<ReadableIssue> history1 = historyManager.getHistory();
        List<ReadableIssue> history2 = historyManager.getHistory();

        assertNotSame(history1, history2, "getHistory should return a new list instance.");
    }

    @Test
    void shouldAddDifferentTaskTypes() {
        Epic epic = new Epic("Epic", "Desc", 2);
        Subtask subtask = new Subtask("Sub", "Desc", 3, Status.NEW, 2);

        historyManager.add(task1);
        historyManager.add(epic);
        historyManager.add(subtask);

        List<ReadableIssue> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(epic));
        assertTrue(history.contains(subtask));
    }
}

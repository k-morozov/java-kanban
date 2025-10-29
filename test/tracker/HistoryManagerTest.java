package tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Задача", "Описание", 1, TaskStatus.NEW);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void shouldAddMultipleTasksToHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", 2, TaskStatus.NEW);
        Task task3 = new Task("Задача 3", "Описание 3", 3, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void shouldLimitHistoryTo10Elements() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Задача " + i, "Описание " + i, i, TaskStatus.NEW);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size());
        assertEquals(6, history.get(0).getId());
        assertEquals(15, history.get(9).getId());
    }

    @Test
    void shouldRemoveOldestTaskWhenLimitExceeded() {
        Task oldTask = new Task("Старая задача", "Описание", 1, TaskStatus.NEW);
        historyManager.add(oldTask);

        for (int i = 2; i <= 11; i++) {
            Task task = new Task("Задача " + i, "Описание " + i, i, TaskStatus.NEW);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size());
        assertFalse(history.contains(oldTask));
        assertEquals(2, history.get(0).getId());
    }

    @Test
    void shouldNotAddNullToHistory() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size());
    }

    @Test
    void shouldReturnCopyOfHistory() {
        Task task = new Task("Задача", "Описание", 1, TaskStatus.NEW);
        historyManager.add(task);

        List<Task> history1 = historyManager.getHistory();
        List<Task> history2 = historyManager.getHistory();

        assertNotSame(history1, history2);
    }

    @Test
    void shouldAddDifferentTaskTypes() {
        Task task = new Task("Задача", "Описание", 1, TaskStatus.NEW);
        Epic epic = new Epic("Эпик", "Описание", 2);
        Subtask subtask = new Subtask("Подзадача", "Описание", 3, TaskStatus.NEW, 2);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
        assertEquals(subtask, history.get(2));
    }

    @Test
    void shouldPreserveTaskDataInHistory() {
        Task task = new Task("Исходное название", "Исходное описание", 1, TaskStatus.NEW);

        historyManager.add(task);

        task.setTitle("Новое название");
        task.setDescription("Новое описание");
        task.setStatus(TaskStatus.DONE);

        List<Task> history = historyManager.getHistory();
        Task taskFromHistory = history.get(0);

        assertEquals(task, taskFromHistory);
    }

    @Test
    void shouldReturnEmptyHistoryInitially() {
        List<Task> history = historyManager.getHistory();

        assertNotNull(history);
        assertEquals(0, history.size());
    }

    @Test
    void shouldAllowDuplicatesInHistory() {
        Task task = new Task("Задача", "Описание", 1, TaskStatus.NEW);

        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
    }
}

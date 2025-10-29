package tracker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager);
        assertInstanceOf(TaskManager.class, manager);

        Task task = manager.createTask("Тестовая задача", "Описание", TaskStatus.NEW);
        assertNotNull(task);
        assertNotNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager);
        assertInstanceOf(HistoryManager.class, historyManager);

        Task task = new Task("Задача", "Описание", 1, TaskStatus.NEW);
        historyManager.add(task);

        assertNotNull(historyManager.getHistory());
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void shouldReturnNewInstanceEachTime() {
        TaskManager manager1 = Managers.getDefault();
        TaskManager manager2 = Managers.getDefault();

        assertNotSame(manager1, manager2);

        Task task1 = manager1.createTask("Задача 1", "Описание", TaskStatus.NEW);

        assertNotNull(manager1.getTask(task1.getId()));
        assertNull(manager2.getTask(task1.getId()));
    }

    @Test
    void shouldReturnNewHistoryManagerInstanceEachTime() {
        HistoryManager history1 = Managers.getDefaultHistory();
        HistoryManager history2 = Managers.getDefaultHistory();

        assertNotSame(history1, history2);

        Task task = new Task("Задача", "Описание", 1, TaskStatus.NEW);
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

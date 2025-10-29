package tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldCreateAndGetTask() {
        Task task = taskManager.createTask("Задача 1", "Описание задачи 1", TaskStatus.NEW);

        assertNotNull(task);
        assertEquals("Задача 1", task.getTitle());
        assertEquals("Описание задачи 1", task.getDescription());
        assertEquals(TaskStatus.NEW, task.getStatus());

        Task retrievedTask = taskManager.getTask(task.getId());
        assertNotNull(retrievedTask);
        assertEquals(task, retrievedTask);
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic epic = taskManager.createEpic("Эпик 1", "Описание эпика 1");

        assertNotNull(epic);
        assertEquals("Эпик 1", epic.getTitle());
        assertEquals(TaskStatus.NEW, epic.getStatus());

        Epic retrievedEpic = taskManager.getEpic(epic.getId());
        assertNotNull(retrievedEpic);
        assertEquals(epic, retrievedEpic);
    }

    @Test
    void shouldCreateAndGetSubtask() {
        Epic epic = taskManager.createEpic("Эпик", "Описание");
        Subtask subtask = taskManager.createSubtask("Подзадача 1", "Описание подзадачи",
                TaskStatus.NEW, epic.getId());

        assertNotNull(subtask);
        assertEquals("Подзадача 1", subtask.getTitle());
        assertEquals(epic.getId(), subtask.getEpicId());

        Subtask retrievedSubtask = taskManager.getSubtask(subtask.getId());
        assertNotNull(retrievedSubtask);
        assertEquals(subtask, retrievedSubtask);
    }

    @Test
    void shouldAddDifferentTaskTypesAndFindById() {
        Task task = taskManager.createTask("Задача", "Описание", TaskStatus.NEW);
        Epic epic = taskManager.createEpic("Эпик", "Описание");
        Subtask subtask = taskManager.createSubtask("Подзадача", "Описание",
                TaskStatus.NEW, epic.getId());

        assertNotNull(taskManager.getTask(task.getId()));
        assertNotNull(taskManager.getEpic(epic.getId()));
        assertNotNull(taskManager.getSubtask(subtask.getId()));

        assertEquals(task, taskManager.getTask(task.getId()));
        assertEquals(epic, taskManager.getEpic(epic.getId()));
        assertEquals(subtask, taskManager.getSubtask(subtask.getId()));
    }

    @Test
    void shouldNotConflictBetweenGeneratedIds() {
        Task task1 = taskManager.createTask("Задача 1", "Описание 1", TaskStatus.NEW);
        Task task2 = taskManager.createTask("Задача 2", "Описание 2", TaskStatus.NEW);
        Epic epic = taskManager.createEpic("Эпик", "Описание");

        assertNotEquals(task1.getId(), task2.getId());
        assertNotEquals(task1.getId(), epic.getId());

        assertEquals(task1, taskManager.getTask(task1.getId()));
        assertEquals(task2, taskManager.getTask(task2.getId()));
        assertEquals(epic, taskManager.getEpic(epic.getId()));
    }

    @Test
    void shouldKeepTaskUnchangedWhenAddedToManager() {
        String originalTitle = "Неизменная задача";
        String originalDescription = "Описание неизменной задачи";
        TaskStatus originalStatus = TaskStatus.NEW;

        Task task = taskManager.createTask(originalTitle, originalDescription, originalStatus);
        int taskId = task.getId();

        Task retrievedTask = taskManager.getTask(taskId);

        assertEquals(originalTitle, retrievedTask.getTitle());
        assertEquals(originalDescription, retrievedTask.getDescription());
        assertEquals(originalStatus, retrievedTask.getStatus());
        assertEquals(taskId, retrievedTask.getId());
    }

    @Test
    void shouldUpdateTask() {
        Task task = taskManager.createTask("Старое название", "Старое описание", TaskStatus.NEW);
        int taskId = task.getId();

        task.setTitle("Новое название");
        task.setDescription("Новое описание");
        task.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.updateTask(task);

        Task updatedTask = taskManager.getTask(taskId);
        assertEquals("Новое название", updatedTask.getTitle());
        assertEquals("Новое описание", updatedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void shouldDeleteTask() {
        Task task = taskManager.createTask("Задача для удаления", "Описание", TaskStatus.NEW);
        int taskId = task.getId();

        assertNotNull(taskManager.getTask(taskId));

        taskManager.deleteTask(taskId);

        assertNull(taskManager.getTask(taskId));
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask("Задача 1", "Описание 1", TaskStatus.NEW);
        taskManager.createTask("Задача 2", "Описание 2", TaskStatus.NEW);

        assertEquals(2, taskManager.getAllTasks().size());

        taskManager.deleteAllTasks();

        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    void shouldGetEpicSubtasks() {
        Epic epic = taskManager.createEpic("Эпик", "Описание");
        Subtask subtask1 = taskManager.createSubtask("Подзадача 1", "Описание 1",
                TaskStatus.NEW, epic.getId());
        Subtask subtask2 = taskManager.createSubtask("Подзадача 2", "Описание 2",
                TaskStatus.NEW, epic.getId());

        ArrayList<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());

        assertEquals(2, epicSubtasks.size());
        assertTrue(epicSubtasks.contains(subtask1));
        assertTrue(epicSubtasks.contains(subtask2));
    }

    @Test
    void shouldReturnNullWhenCreatingSubtaskWithNonExistentEpic() {
        Subtask subtask = taskManager.createSubtask("Подзадача", "Описание",
                TaskStatus.NEW, 999);

        assertNull(subtask);
    }

    @Test
    void shouldUpdateEpicStatusWhenSubtasksChange() {
        Epic epic = taskManager.createEpic("Эпик", "Описание");
        Subtask subtask1 = taskManager.createSubtask("Подзадача 1", "Описание 1",
                TaskStatus.NEW, epic.getId());
        Subtask subtask2 = taskManager.createSubtask("Подзадача 2", "Описание 2",
                TaskStatus.NEW, epic.getId());

        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatus.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task = taskManager.createTask("Задача", "Описание", TaskStatus.NEW);
        Epic epic = taskManager.createEpic("Эпик", "Описание");

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
    }

    @Test
    void shouldLimitHistoryTo10Elements() {
        for (int i = 0; i < 15; i++) {
            Task task = taskManager.createTask("Задача " + i, "Описание", TaskStatus.NEW);
            taskManager.getTask(task.getId());
        }

        List<Task> history = taskManager.getHistory();

        assertEquals(10, history.size());
    }

    @Test
    void shouldDeleteEpicWithSubtasks() {
        Epic epic = taskManager.createEpic("Эпик", "Описание");
        Subtask subtask1 = taskManager.createSubtask("Подзадача 1", "Описание",
                TaskStatus.NEW, epic.getId());
        Subtask subtask2 = taskManager.createSubtask("Подзадача 2", "Описание",
                TaskStatus.NEW, epic.getId());

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()));
        assertNull(taskManager.getSubtask(subtask1.getId()));
        assertNull(taskManager.getSubtask(subtask2.getId()));
    }

    @Test
    void shouldDeleteSubtask() {
        Epic epic = taskManager.createEpic("Эпик", "Описание");
        Subtask subtask = taskManager.createSubtask("Подзадача", "Описание",
                TaskStatus.NEW, epic.getId());

        assertEquals(1, taskManager.getEpicSubtasks(epic.getId()).size());

        taskManager.deleteSubtask(subtask.getId());

        assertNull(taskManager.getSubtask(subtask.getId()));
        assertEquals(0, taskManager.getEpicSubtasks(epic.getId()).size());
    }

    @Test
    void shouldDeleteAllSubtasks() {
        Epic epic = taskManager.createEpic("Эпик", "Описание");
        taskManager.createSubtask("Подзадача 1", "Описание", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask("Подзадача 2", "Описание", TaskStatus.DONE, epic.getId());

        assertEquals(TaskStatus.DONE, taskManager.getEpic(epic.getId()).getStatus());

        taskManager.deleteAllSubtasks();

        assertEquals(0, taskManager.getAllSubtasks().size());
        assertEquals(0, taskManager.getEpicSubtasks(epic.getId()).size());
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }
}

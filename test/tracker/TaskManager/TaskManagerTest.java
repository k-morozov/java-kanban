package tracker.TaskManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tracker.Managers;
import tracker.issue.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldCreateAndGetTask() {
        TaskView task = taskManager.createTask("Задача 1", "Описание задачи 1", Status.NEW);

        assertNotNull(task);
        assertEquals("Задача 1", task.getTitle());
        assertEquals("Описание задачи 1", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());

        TaskView retrievedTask = taskManager.getTask(task.getId());
        assertNotNull(retrievedTask);
        assertEquals(task, retrievedTask);
    }

    @Test
    void shouldCreateAndGetEpic() {
        EpicView epic = taskManager.createEpic("Эпик 1", "Описание эпика 1");

        assertNotNull(epic);
        assertEquals("Эпик 1", epic.getTitle());
        assertEquals(Status.NEW, epic.getStatus());

        EpicView retrievedEpic = taskManager.getEpic(epic.getId());
        assertNotNull(retrievedEpic);
        assertEquals(epic, retrievedEpic);
    }

    @Test
    void shouldCreateAndGetSubtask() {
        EpicView epic = taskManager.createEpic("Эпик", "Описание");
        SubtaskView subtask = taskManager.createSubtask("Подзадача 1", "Описание подзадачи",
                Status.NEW, epic.getId());

        assertNotNull(subtask);
        assertEquals("Подзадача 1", subtask.getTitle());
        assertEquals(epic.getId(), subtask.getEpicId());

        SubtaskView retrievedSubtask = taskManager.getSubtask(subtask.getId());
        assertNotNull(retrievedSubtask);
        assertEquals(subtask, retrievedSubtask);
    }

    @Test
    void shouldAddDifferentTaskTypesAndFindById() {
        TaskView task = taskManager.createTask("Задача", "Описание", Status.NEW);
        EpicView epic = taskManager.createEpic("Эпик", "Описание");
        SubtaskView subtask = taskManager.createSubtask("Подзадача", "Описание",
                Status.NEW, epic.getId());

        assertNotNull(taskManager.getTask(task.getId()));
        assertNotNull(taskManager.getEpic(epic.getId()));
        assertNotNull(taskManager.getSubtask(subtask.getId()));

        assertEquals(task, taskManager.getTask(task.getId()));
        assertEquals(epic, taskManager.getEpic(epic.getId()));
        assertEquals(subtask, taskManager.getSubtask(subtask.getId()));
    }

    @Test
    void shouldNotConflictBetweenGeneratedIds() {
        TaskView task1 = taskManager.createTask("Задача 1", "Описание 1", Status.NEW);
        TaskView task2 = taskManager.createTask("Задача 2", "Описание 2", Status.NEW);
        EpicView epic = taskManager.createEpic("Эпик", "Описание");

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
        Status originalStatus = Status.NEW;

        TaskView task = taskManager.createTask(originalTitle, originalDescription, originalStatus);
        int taskId = task.getId();

        TaskView retrievedTask = taskManager.getTask(taskId);

        assertEquals(originalTitle, retrievedTask.getTitle());
        assertEquals(originalDescription, retrievedTask.getDescription());
        assertEquals(originalStatus, retrievedTask.getStatus());
        assertEquals(taskId, retrievedTask.getId());
    }

    @Test
    void shouldUpdateTask() {
        TaskView task = taskManager.createTask("Старое название", "Старое описание", Status.NEW);
        int taskId = task.getId();

        taskManager.updateTask(new Task("Новое название", "Новое описание", taskId, Status.IN_PROGRESS));

        TaskView updatedTask = taskManager.getTask(taskId);
        assertEquals("Новое название", updatedTask.getTitle());
        assertEquals("Новое описание", updatedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void shouldDeleteTask() {
        TaskView task = taskManager.createTask("Задача для удаления", "Описание", Status.NEW);
        int taskId = task.getId();

        assertNotNull(taskManager.getTask(taskId));

        taskManager.deleteTask(taskId);

        assertNull(taskManager.getTask(taskId));
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask("Задача 1", "Описание 1", Status.NEW);
        taskManager.createTask("Задача 2", "Описание 2", Status.NEW);

        assertEquals(2, taskManager.getAllTasks().size());

        taskManager.deleteAllTasks();

        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    void shouldGetEpicSubtasks() {
        EpicView epic = taskManager.createEpic("Эпик", "Описание");
        SubtaskView subtask1 = taskManager.createSubtask("Подзадача 1", "Описание 1",
                Status.NEW, epic.getId());
        SubtaskView subtask2 = taskManager.createSubtask("Подзадача 2", "Описание 2",
                Status.NEW, epic.getId());

        List<SubtaskView> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());

        assertEquals(2, epicSubtasks.size());
        assertEquals(subtask1.getId(), epicSubtasks.get(0).getId());
        assertEquals(subtask2.getId(), epicSubtasks.get(1).getId());
    }

    @Test
    void shouldReturnNullWhenCreatingSubtaskWithNonExistentEpic() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> taskManager.createSubtask("Подзадача", "Описание",
                Status.NEW, 999));
    }

    @Test
    void shouldUpdateEpicStatusWhenSubtasksChange() {
        EpicView epic = taskManager.createEpic("Эпик", "Описание");
        SubtaskView subtask1 = taskManager.createSubtask("Подзадача 1", "Описание 1",
                Status.NEW, epic.getId());
        SubtaskView subtask2 = taskManager.createSubtask("Подзадача 2", "Описание 2",
                Status.NEW, epic.getId());

        assertEquals(Status.NEW, taskManager.getEpic(epic.getId()).getStatus());

        taskManager.updateSubtask(new Subtask("Подзадача 1", "Описание 1",
                subtask1.getId(),
                Status.IN_PROGRESS, epic.getId()));

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus());

        taskManager.updateSubtask(new Subtask("Подзадача 1", "Описание 1",
                subtask1.getId(),
                Status.DONE, epic.getId()));
        taskManager.updateSubtask(new Subtask("Подзадача 1", "Описание 1",
                subtask2.getId(),
                Status.DONE, epic.getId()));

        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldAddTasksToHistory() {
        TaskView task = taskManager.createTask("Задача", "Описание", Status.NEW);
        EpicView epic = taskManager.createEpic("Эпик", "Описание");

        TaskView r1 = taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());

        List<ReadableIssue> history = taskManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
    }

    @Test
    void shouldRemoveTasksFromHistory() {
        TaskView task = taskManager.createTask("Задача", "Описание", Status.NEW);
        EpicView epic = taskManager.createEpic("Эпик", "Описание");

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.deleteTask(task.getId());

        List<ReadableIssue> history = taskManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(epic.getId(), history.get(0).getId());

        taskManager.deleteTask(epic.getId());
        history = taskManager.getHistory();

        assertTrue(history.isEmpty());
    }

    @Deprecated
    @Disabled
    @Test
    void shouldLimitHistoryTo10Elements() {
        for (int i = 0; i < 15; i++) {
            TaskView task = taskManager.createTask("Задача " + i, "Описание", Status.NEW);
            taskManager.getTask(task.getId());
        }

        List<ReadableIssue> history = taskManager.getHistory();

        assertEquals(10, history.size());
    }

    @Test
    void shouldDeleteEpicWithSubtasks() {
        EpicView epic = taskManager.createEpic("Эпик", "Описание");
        SubtaskView subtask1 = taskManager.createSubtask("Подзадача 1", "Описание",
                Status.NEW, epic.getId());
        SubtaskView subtask2 = taskManager.createSubtask("Подзадача 2", "Описание",
                Status.NEW, epic.getId());

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()));
        assertNull(taskManager.getSubtask(subtask1.getId()));
        assertNull(taskManager.getSubtask(subtask2.getId()));
    }

    @Test
    void shouldDeleteSubtask() {
        EpicView epic = taskManager.createEpic("Эпик", "Описание");
        SubtaskView subtask = taskManager.createSubtask("Подзадача", "Описание",
                Status.NEW, epic.getId());

        assertEquals(1, taskManager.getEpicSubtasks(epic.getId()).size());

        taskManager.deleteSubtask(subtask.getId());

        assertNull(taskManager.getSubtask(subtask.getId()));
        assertEquals(0, taskManager.getEpicSubtasks(epic.getId()).size());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldDeleteAllSubtasks() {
        EpicView epic = taskManager.createEpic("Эпик", "Описание");
        taskManager.createSubtask("Подзадача 1", "Описание", Status.DONE, epic.getId());
        taskManager.createSubtask("Подзадача 2", "Описание", Status.DONE, epic.getId());

        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus());

        taskManager.deleteAllSubtasks();

        assertEquals(0, taskManager.getAllSubtasks().size());
        assertEquals(0, taskManager.getEpicSubtasks(epic.getId()).size());
        assertEquals(Status.NEW, taskManager.getEpic(epic.getId()).getStatus());
    }
}

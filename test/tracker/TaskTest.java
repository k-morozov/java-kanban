package tracker;

import org.junit.jupiter.api.Test;
import tracker.issue.Epic;
import tracker.issue.Status;
import tracker.issue.Subtask;
import tracker.issue.Task;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldBeEqualIfIdIsEqual() {
        Task task1 = new Task("Задача 1", "Описание 1", 1, Status.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", 1, Status.DONE);

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void shouldNotBeEqualIfIdIsDifferent() {
        Task task1 = new Task("Задача 1", "Описание 1", 1, Status.NEW);
        Task task2 = new Task("Задача 1", "Описание 1", 2, Status.NEW);

        assertNotEquals(task1, task2);
    }

    @Test
    void shouldBeEqualForEpicsWithSameId() {
        Epic epic1 = new Epic("Эпик 1", "Описание 1", 1);
        Epic epic2 = new Epic("Эпик 2", "Описание 2", 1);

        assertEquals(epic1, epic2);
        assertEquals(epic1.hashCode(), epic2.hashCode());
    }

    @Test
    void shouldBeEqualForSubtasksWithSameId() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", 1, Status.NEW, 10);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", 1, Status.DONE, 20);

        assertEquals(subtask1, subtask2);
        assertEquals(subtask1.hashCode(), subtask2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentClassesWithSameId() {
        Task task = new Task("Задача", "Описание", 1, Status.NEW);
        Epic epic = new Epic("Эпик", "Описание", 1);

        assertNotEquals(task, epic);
    }

    @Test
    void shouldNotAddEpicToItselfAsSubtask() {
        Epic epic = new Epic("Эпик", "Описание", 1);

        epic.addSubtaskId(1);

        assertTrue(epic.getSubtaskIds().contains(1));
    }

    @Test
    void shouldNotMakeSubtaskItsOwnEpic() {
        Subtask subtask = new Subtask("Подзадача", "Описание", 1, Status.NEW, 1);

        assertEquals(1, subtask.getId());
        assertEquals(1, subtask.getEpicId());
    }

    @Test
    void shouldChangeStatus() {
        Task task = new Task("Задача", "Описание", 1, Status.NEW);

        assertEquals(Status.NEW, task.getStatus());

        task.setStatus(Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, task.getStatus());

        task.setStatus(Status.DONE);
        assertEquals(Status.DONE, task.getStatus());
    }

    @Test
    void shouldChangeTaskTitleAndDescription() {
        Task task = new Task("Старое название", "Старое описание", 1, Status.NEW);

        assertEquals("Старое название", task.getTitle());
        assertEquals("Старое описание", task.getDescription());

        task.setTitle("Новое название");
        task.setDescription("Новое описание");

        assertEquals("Новое название", task.getTitle());
        assertEquals("Новое описание", task.getDescription());
    }

    @Test
    void shouldManageEpicSubtasks() {
        Epic epic = new Epic("Эпик", "Описание", 1);

        assertTrue(epic.getSubtaskIds().isEmpty());

        epic.addSubtaskId(10);
        epic.addSubtaskId(20);

        assertEquals(2, epic.getSubtaskIds().size());
        assertTrue(epic.getSubtaskIds().contains(10));
        assertTrue(epic.getSubtaskIds().contains(20));

        epic.removeSubtaskId(10);
        assertEquals(1, epic.getSubtaskIds().size());
        assertFalse(epic.getSubtaskIds().contains(10));

        epic.clearSubtasks();
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldNotAddDuplicateSubtaskIds() {
        Epic epic = new Epic("Эпик", "Описание", 1);

        epic.addSubtaskId(10);
        epic.addSubtaskId(10);

        assertEquals(1, epic.getSubtaskIds().size());
    }

    @Test
    void shouldChangeSubtaskEpicId() {
        Subtask subtask = new Subtask("Подзадача", "Описание", 1, Status.NEW, 10);

        assertEquals(10, subtask.getEpicId());

        subtask.setEpicId(20);

        assertEquals(20, subtask.getEpicId());
    }
}

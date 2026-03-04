package tracker.TaskManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.issue.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private final static Path path = Path.of("test_backup.csv");
    private TaskManager tm;

    public static class BrokenFileException extends RuntimeException {
        public BrokenFileException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    @BeforeEach
    void setUp() {
        reloadTaskManager();
    }

    @AfterEach
    void tearDown() {
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new BrokenFileException(e.getMessage(), e);
        }
    }

    void reloadTaskManager() {
        this.tm = new FileBackedTaskManager(path);
    }

    @Test
    void emptyManager() {
        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                """,
                text);

        reloadTaskManager();

        text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                """,
                text);
    }

    @Test
    void createOneTask() {
        tm.createTask("task1", "description1", Status.NEW);

        TaskView task1 = tm.getTask(1);
        assertEquals("task1", task1.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,TASK,task1,NEW,description1
                """,
                text);

        reloadTaskManager();
        assertEquals("task1", task1.getTitle());

        text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,TASK,task1,NEW,description1
                """,
                text);
    }

    @Test
    void createOneTaskAfterReload() {
        tm.createTask("task1", "description1", Status.NEW);

        TaskView task1 = tm.getTask(1);
        assertEquals("task1", task1.getTitle());

        reloadTaskManager();
        assertEquals("task1", task1.getTitle());

        tm.createTask("task2", "description2", Status.DONE);
        TaskView task2 = tm.getTask(2);

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,TASK,task1,NEW,description1
                2,TASK,task2,DONE,description2
                """,
                text);

        reloadTaskManager();
        assertEquals("task1", task1.getTitle());
        assertEquals("task2", task2.getTitle());
    }

    @Test
    void updateTaskBeforeReload() {
        tm.createTask("task1", "description1", Status.NEW);

        TaskView task1 = tm.getTask(1);

        Task task1Updated = new Task("task1Updated", "description1", task1.getId(), Status.NEW);

        tm.updateTask(task1Updated);

        reloadTaskManager();
        assertEquals("task1Updated", task1Updated.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,TASK,task1Updated,NEW,description1
                """,
                text);
    }

    @Test
    void updateTaskAfterReload() {
        tm.createTask("task1", "description1", Status.NEW);

        TaskView task1 = tm.getTask(1);

        reloadTaskManager();

        Task task1Updated = new Task("task1Updated", "description1", task1.getId(), Status.NEW);
        tm.updateTask(task1Updated);

        assertEquals("task1Updated", task1Updated.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,TASK,task1Updated,NEW,description1
                """,
                text);
    }

    @Test
    void deleteTaskAfterReload() {
        tm.createTask("task1", "description1", Status.NEW);
        tm.createTask("task2", "description2", Status.IN_PROGRESS);

        TaskView task1 = tm.getTask(1);

        reloadTaskManager();

        tm.deleteTask(task1.getId());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                2,TASK,task2,IN_PROGRESS,description2
                """,
                text);
    }

    @Test
    void deleteAllTaskAfterReload() {
        tm.createTask("task1", "description1", Status.NEW);
        tm.createTask("task2", "description2", Status.IN_PROGRESS);

        reloadTaskManager();

        tm.deleteAllTasks();

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                """,
                text);
    }

    @Test
    void createOneEpic() {
        tm.createEpic("epic1", "description1");

        EpicView epic1 = tm.getEpic(1);
        assertEquals("epic1", epic1.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,NEW,description1
                """,
                text);

        reloadTaskManager();

        epic1 = tm.getEpic(1);
        assertEquals("epic1", epic1.getTitle());

        text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,NEW,description1
                """,
                text);
    }

    @Test
    void createOneEpicAfterReload() {
        tm.createEpic("epic1", "description1");

        EpicView epic1 = tm.getEpic(1);
        assertEquals("epic1", epic1.getTitle());

        reloadTaskManager();

        epic1 = tm.getEpic(1);
        assertEquals("epic1", epic1.getTitle());

        tm.createEpic("epic2", "description2");
        EpicView epic2 = tm.getEpic(2);

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,NEW,description1
                2,EPIC,epic2,NEW,description2
                """,
                text);

        reloadTaskManager();
        assertEquals("epic1", epic1.getTitle());
        assertEquals("epic2", epic2.getTitle());
    }

    @Test
    void updateEpicBeforeReload() {
        tm.createEpic("epic1", "description1");

        EpicView epic1 = tm.getEpic(1);

        Epic updated = new Epic("epicUpdated", "description2", epic1.getId());
        tm.updateEpic(updated);

        reloadTaskManager();

        EpicView updatedView = tm.getEpic(1);
        assertEquals("epicUpdated", updatedView.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epicUpdated,NEW,description2
                """,
                text);
    }

    @Test
    void updateEpicAfterReload() {
        tm.createEpic("epic1", "description1");

        EpicView epic1 = tm.getEpic(1);

        reloadTaskManager();

        Epic updated = new Epic("epicUpdated", "description2", epic1.getId());
        tm.updateEpic(updated);

        assertEquals("epicUpdated", updated.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epicUpdated,NEW,description2
                """,
                text);
    }

    @Test
    void deleteEpicAfterReload() {
        tm.createEpic("epic1", "description1");
        tm.createEpic("epic2", "description2");

        EpicView epic1 = tm.getEpic(1);

        reloadTaskManager();

        tm.deleteEpic(epic1.getId());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                2,EPIC,epic2,NEW,description2
                """,
                text);
    }

    @Test
    void deleteAllEpicAfterReload() {
        tm.createEpic("epic1", "description1");
        tm.createEpic("epic2", "description2");

        reloadTaskManager();

        tm.deleteAllEpics();

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                """,
                text);
    }

    @Test
    void createOneSubtask() {
        tm.createEpic("epic1", "description1");
        tm.createSubtask("subtask1", "description1", Status.NEW, 1);

        SubtaskView subtask1 = tm.getSubtask(2);
        assertEquals("subtask1", subtask1.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,NEW,description1
                2,SUBTASK,subtask1,NEW,description1,1
                """,
                text);

        reloadTaskManager();

        subtask1 = tm.getSubtask(2);
        assertEquals("subtask1", subtask1.getTitle());

        text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,NEW,description1
                2,SUBTASK,subtask1,NEW,description1,1
                """,
                text);
    }

    @Test
    void createOneSubtaskAfterReload() {
        tm.createEpic("epic1", "description1");
        tm.createSubtask("subtask1", "description1", Status.NEW, 1);

        SubtaskView subtask1 = tm.getSubtask(2);
        assertEquals("subtask1", subtask1.getTitle());

        reloadTaskManager();

        subtask1 = tm.getSubtask(2);
        assertEquals("subtask1", subtask1.getTitle());

        tm.createSubtask("subtask2", "description2", Status.NEW, 1);
        SubtaskView subtask2 = tm.getSubtask(3);

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,NEW,description1
                2,SUBTASK,subtask1,NEW,description1,1
                3,SUBTASK,subtask2,NEW,description2,1
                """,
                text);

        reloadTaskManager();
        assertEquals("subtask1", subtask1.getTitle());
        assertEquals("subtask2", subtask2.getTitle());
    }

    @Test
    void updateSubtaskBeforeReload() {
        tm.createEpic("epic1", "description1");
        tm.createSubtask("subtask1", "description1", Status.NEW, 1);

        SubtaskView subtask1 = tm.getSubtask(2);

        Subtask updated = new Subtask("subtaskUpdated", "description2", subtask1.getId(), Status.IN_PROGRESS, 1);
        tm.updateSubtask(updated);

        reloadTaskManager();

        SubtaskView updatedSubtask = tm.getSubtask(2);
        assertEquals("subtaskUpdated", updatedSubtask.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,IN_PROGRESS,description1
                2,SUBTASK,subtaskUpdated,IN_PROGRESS,description2,1
                """,
                text);
    }

    @Test
    void updateSubtaskAfterReload() {
        tm.createEpic("epic1", "description1");
        tm.createSubtask("subtask1", "description1", Status.NEW, 1);

        reloadTaskManager();

        SubtaskView subtask1 = tm.getSubtask(2);

        Subtask updated = new Subtask("subtaskUpdated", "description2", subtask1.getId(), Status.IN_PROGRESS, 1);
        tm.updateSubtask(updated);

        SubtaskView updatedSubtask = tm.getSubtask(2);
        assertEquals("subtaskUpdated", updatedSubtask.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,IN_PROGRESS,description1
                2,SUBTASK,subtaskUpdated,IN_PROGRESS,description2,1
                """,
                text);
    }

    @Test
    void deleteSubtaskAfterReload() {
        tm.createEpic("epic1", "description1");
        tm.createSubtask("subtask1", "description1", Status.NEW, 1);
        tm.createSubtask("subtask2", "description2", Status.NEW, 1);

        reloadTaskManager();

        SubtaskView subtask1 = tm.getSubtask(2);

        tm.deleteSubtask(subtask1.getId());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,epic
                1,EPIC,epic1,NEW,description1
                3,SUBTASK,subtask2,NEW,description2,1
                """,
                text);
    }

    @Test
    void deleteAllSubtaskAfterReload() {
        tm.createEpic("epic1", "description1");
        tm.createSubtask("subtask1", "description1", Status.NEW, 1);
        tm.createSubtask("subtask2", "description2", Status.NEW, 1);

        reloadTaskManager();

        SubtaskView subtask1 = tm.getSubtask(2);

        tm.deleteSubtask(subtask1.getId());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                        id,type,name,status,description,epic
                        1,EPIC,epic1,NEW,description1
                        3,SUBTASK,subtask2,NEW,description2,1
                        """,
                text);
    }
}
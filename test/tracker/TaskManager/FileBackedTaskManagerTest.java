package tracker.TaskManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.issue.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest {
    private final static Path path = Path.of("test_backup.csv");

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
        this.taskManager = new FileBackedTaskManager(path);
    }

    @Test
    void emptyManager() {
        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                """,
                text);

        reloadTaskManager();

        text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                """,
                text);
    }

    @Test
    void createOneTask() {
        taskManager.createTask("task1", "description1", Status.NEW,
                defaultStartTime, defaultDuration);

        TaskView task1 = taskManager.getTask(1);
        assertEquals("task1", task1.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,TASK,task1,NEW,description1,2007-12-03T10:15:30,PT10S
                """,
                text);

        reloadTaskManager();
        assertEquals("task1", task1.getTitle());

        text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,TASK,task1,NEW,description1,2007-12-03T10:15:30,PT10S
                """,
                text);
    }

    @Test
    void createOneTaskAfterReload() {
        taskManager.createTask("task1", "description1", Status.NEW, defaultStartTime, defaultDuration);

        TaskView task1 = taskManager.getTask(1);
        assertEquals("task1", task1.getTitle());

        reloadTaskManager();
        assertEquals("task1", task1.getTitle());

        taskManager.createTask("task2", "description2", Status.DONE,
                LocalDateTime.parse("2007-12-03T10:15:40."), defaultDuration);
        TaskView task2 = taskManager.getTask(2);

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,TASK,task1,NEW,description1,2007-12-03T10:15:30,PT10S
                2,TASK,task2,DONE,description2,2007-12-03T10:15:40,PT10S
                """,
                text);

        reloadTaskManager();
        assertEquals("task1", task1.getTitle());
        assertEquals("task2", task2.getTitle());
    }

    @Test
    void updateTaskBeforeReload() {
        taskManager.createTask("task1", "description1", Status.NEW,
                LocalDateTime.parse("2007-12-03T10:15:30."), defaultDuration);

        TaskView task1 = taskManager.getTask(1);

        Task task1Updated = new Task("task1Updated", "description1", task1.getId(), Status.NEW,
                defaultStartTime, Duration.ofSeconds(30, 0));

        taskManager.updateTask(task1Updated);

        reloadTaskManager();

        task1 = taskManager.getTask(1);
        assertEquals("task1Updated", task1.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,TASK,task1Updated,NEW,description1,2007-12-03T10:15:30,PT30S
                """,
                text);

        assertEquals(defaultStartTime, task1.getStartTime().orElseThrow());
        assertEquals(Duration.ofSeconds(30, 0), task1.getDuration().orElseThrow());
    }

    @Test
    void updateTaskAfterReload() {
        taskManager.createTask("task1", "description1", Status.NEW,
                defaultStartTime, defaultDuration);

        TaskView task1 = taskManager.getTask(1);

        reloadTaskManager();

        Task task1Updated = new Task("task1Updated", "description1", task1.getId(), Status.NEW,
                defaultStartTime, defaultDuration);
        taskManager.updateTask(task1Updated);

        assertEquals("task1Updated", task1Updated.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,TASK,task1Updated,NEW,description1,2007-12-03T10:15:30,PT10S
                """,
                text);
    }

    @Test
    void deleteTaskAfterReload() {
        taskManager.createTask("task1", "description1", Status.NEW, defaultStartTime, defaultDuration);
        taskManager.createTask("task2", "description2", Status.IN_PROGRESS,
                LocalDateTime.parse("2007-12-03T10:15:40."), defaultDuration);

        TaskView task1 = taskManager.getTask(1);

        reloadTaskManager();

        taskManager.deleteTask(task1.getId());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                2,TASK,task2,IN_PROGRESS,description2,2007-12-03T10:15:40,PT10S
                """,
                text);
    }

    @Test
    void deleteAllTaskAfterReload() {
        taskManager.createTask("task1", "description1", Status.NEW, defaultStartTime, defaultDuration);
        taskManager.createTask("task2", "description2", Status.IN_PROGRESS,
                LocalDateTime.parse("2007-12-03T10:15:40."), defaultDuration);

        reloadTaskManager();

        taskManager.deleteAllTasks();

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                """,
                text);
    }

    @Test
    void createOneEpic() {
        taskManager.createEpic("epic1", "description1");

        EpicView epic1 = taskManager.getEpic(1);
        assertEquals("epic1", epic1.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epic1,NEW,description1
                """,
                text);

        reloadTaskManager();

        epic1 = taskManager.getEpic(1);
        assertEquals("epic1", epic1.getTitle());

        text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epic1,NEW,description1
                """,
                text);
    }

    @Test
    void createOneEpicAfterReload() {
        taskManager.createEpic("epic1", "description1");

        EpicView epic1 = taskManager.getEpic(1);
        assertEquals("epic1", epic1.getTitle());

        reloadTaskManager();

        epic1 = taskManager.getEpic(1);
        assertEquals("epic1", epic1.getTitle());

        taskManager.createEpic("epic2", "description2");
        EpicView epic2 = taskManager.getEpic(2);

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
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
        taskManager.createEpic("epic1", "description1");

        EpicView epic1 = taskManager.getEpic(1);

        Epic updated = new Epic("epicUpdated", "description2", epic1.getId());
        taskManager.updateEpic(updated);

        reloadTaskManager();

        EpicView updatedView = taskManager.getEpic(1);
        assertEquals("epicUpdated", updatedView.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epicUpdated,NEW,description2
                """,
                text);
    }

    @Test
    void updateEpicAfterReload() {
        taskManager.createEpic("epic1", "description1");

        EpicView epic1 = taskManager.getEpic(1);

        reloadTaskManager();

        Epic updated = new Epic("epicUpdated", "description2", epic1.getId());
        taskManager.updateEpic(updated);

        assertEquals("epicUpdated", updated.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epicUpdated,NEW,description2
                """,
                text);
    }

    @Test
    void deleteEpicAfterReload() {
        taskManager.createEpic("epic1", "description1");
        taskManager.createEpic("epic2", "description2");

        EpicView epic1 = taskManager.getEpic(1);

        reloadTaskManager();

        taskManager.deleteEpic(epic1.getId());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                2,EPIC,epic2,NEW,description2
                """,
                text);
    }

    @Test
    void deleteAllEpicAfterReload() {
        taskManager.createEpic("epic1", "description1");
        taskManager.createEpic("epic2", "description2");

        reloadTaskManager();

        taskManager.deleteAllEpics();

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                """,
                text);
    }

    @Test
    void createOneSubtask() {
        taskManager.createEpic("epic1", "description1");
        taskManager.createSubtask("subtask1", "description1", Status.NEW, defaultStartTime, defaultDuration, 1);

        SubtaskView subtask1 = taskManager.getSubtask(2);
        assertEquals("subtask1", subtask1.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epic1,NEW,description1,2007-12-03T10:15:30,PT10S
                2,SUBTASK,subtask1,NEW,description1,2007-12-03T10:15:30,PT10S,1
                """,
                text);

        reloadTaskManager();

        EpicView epic = taskManager.getEpic(1);

        assertEquals(defaultStartTime, epic.getStartTime().orElseThrow());

        subtask1 = taskManager.getSubtask(2);
        assertEquals("subtask1", subtask1.getTitle());

        text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epic1,NEW,description1,2007-12-03T10:15:30,PT10S
                2,SUBTASK,subtask1,NEW,description1,2007-12-03T10:15:30,PT10S,1
                """,
                text);
    }

    @Test
    void createOneSubtaskAfterReload() {
        taskManager.createEpic("epic1", "description1");
        taskManager.createSubtask("subtask1", "description1", Status.NEW, defaultStartTime, defaultDuration,1);

        SubtaskView subtask1 = taskManager.getSubtask(2);
        assertEquals("subtask1", subtask1.getTitle());

        reloadTaskManager();

        subtask1 = taskManager.getSubtask(2);
        assertEquals("subtask1", subtask1.getTitle());

        taskManager.createSubtask("subtask2", "description2", Status.NEW,
                LocalDateTime.parse("2007-12-03T10:15:40."), defaultDuration,1);
        SubtaskView subtask2 = taskManager.getSubtask(3);

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epic1,NEW,description1,2007-12-03T10:15:30,PT20S
                2,SUBTASK,subtask1,NEW,description1,2007-12-03T10:15:30,PT10S,1
                3,SUBTASK,subtask2,NEW,description2,2007-12-03T10:15:40,PT10S,1
                """,
                text);

        reloadTaskManager();
        assertEquals("subtask1", subtask1.getTitle());
        assertEquals("subtask2", subtask2.getTitle());
    }

    @Test
    void updateSubtaskBeforeReload() {
        taskManager.createEpic("epic1", "description1");
        taskManager.createSubtask("subtask1", "description1", Status.NEW, defaultStartTime, defaultDuration,1);

        SubtaskView subtask1 = taskManager.getSubtask(2);

        Subtask updated = new Subtask("subtaskUpdated", "description2", subtask1.getId(), Status.IN_PROGRESS,
                LocalDateTime.parse("2008-12-03T10:15:30."), Duration.ofSeconds(20, 0),1);
        taskManager.updateSubtask(updated);

        reloadTaskManager();

        SubtaskView updatedSubtask = taskManager.getSubtask(2);
        assertEquals("subtaskUpdated", updatedSubtask.getTitle());
        assertEquals(LocalDateTime.parse("2008-12-03T10:15:30."), updatedSubtask.getStartTime().orElseThrow());
        assertEquals(Duration.ofSeconds(20, 0), updatedSubtask.getDuration().orElseThrow());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epic1,IN_PROGRESS,description1,2008-12-03T10:15:30,PT20S
                2,SUBTASK,subtaskUpdated,IN_PROGRESS,description2,2008-12-03T10:15:30,PT20S,1
                """,
                text);
    }

    @Test
    void updateSubtaskAfterReload() {
        taskManager.createEpic("epic1", "description1");
        taskManager.createSubtask("subtask1", "description1", Status.NEW, defaultStartTime, defaultDuration,1);

        reloadTaskManager();

        SubtaskView subtask1 = taskManager.getSubtask(2);

        Subtask updated = new Subtask("subtaskUpdated", "description2", subtask1.getId(), Status.IN_PROGRESS,
                defaultStartTime, Duration.ofSeconds(20, 0),1);
        taskManager.updateSubtask(updated);

        SubtaskView updatedSubtask = taskManager.getSubtask(2);
        assertEquals("subtaskUpdated", updatedSubtask.getTitle());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epic1,IN_PROGRESS,description1,2007-12-03T10:15:30,PT20S
                2,SUBTASK,subtaskUpdated,IN_PROGRESS,description2,2007-12-03T10:15:30,PT20S,1
                """,
                text);
    }

    @Test
    void deleteSubtaskAfterReload() {
        taskManager.createEpic("epic1", "description1");
        taskManager.createSubtask("subtask1", "description1", Status.NEW,
                LocalDateTime.parse("2007-12-03T10:15:30."), defaultDuration,1);
        taskManager.createSubtask("subtask2", "description2", Status.NEW,
                LocalDateTime.parse("2007-12-03T10:15:40."), defaultDuration,1);

        reloadTaskManager();

        SubtaskView subtask1 = taskManager.getSubtask(2);

        taskManager.deleteSubtask(subtask1.getId());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                id,type,name,status,description,start_ts,duration,epic
                1,EPIC,epic1,NEW,description1,2007-12-03T10:15:40,PT10S
                3,SUBTASK,subtask2,NEW,description2,2007-12-03T10:15:40,PT10S,1
                """,
                text);
    }

    @Test
    void deleteAllSubtaskAfterReload() {
        taskManager.createEpic("epic1", "description1");
        taskManager.createSubtask("subtask1", "description1", Status.NEW, defaultStartTime, defaultDuration,1);
        taskManager.createSubtask("subtask2", "description2", Status.NEW,
                LocalDateTime.parse("2007-12-03T10:15:40."), defaultDuration,1);

        reloadTaskManager();

        SubtaskView subtask1 = taskManager.getSubtask(2);

        taskManager.deleteSubtask(subtask1.getId());

        String text = assertDoesNotThrow(() -> Files.readString(path));
        assertEquals("""
                        id,type,name,status,description,start_ts,duration,epic
                        1,EPIC,epic1,NEW,description1,2007-12-03T10:15:40,PT10S
                        3,SUBTASK,subtask2,NEW,description2,2007-12-03T10:15:40,PT10S,1
                        """,
                text);
    }
}
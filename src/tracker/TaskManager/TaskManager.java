package tracker.TaskManager;

import tracker.issue.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {

    TaskView createTask(String title, String description, Status status, LocalDateTime startTime, Duration duration);

    TaskView getTask(int id);

    List<TaskView> getAllTasks();

    void updateTask(Task task);

    void deleteTask(int id);

    void deleteAllTasks();

    EpicView createEpic(String title, String description);

    EpicView getEpic(int id);

    List<EpicView> getAllEpics();

    void updateEpic(Epic epic);

    void deleteEpic(int id);

    void deleteAllEpics();

    SubtaskView createSubtask(String title, String description, Status status, LocalDateTime startTime, Duration duration, int epicId);

    SubtaskView getSubtask(int id);

    List<SubtaskView> getAllSubtasks();

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    void deleteAllSubtasks();

    List<SubtaskView> getEpicSubtasks(int epicId);

    List<ReadableIssue> getHistory();

    List<ReadableIssue> getPrioritizedTasks();
}
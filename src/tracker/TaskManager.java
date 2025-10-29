package tracker;

import java.util.ArrayList;

public interface TaskManager {

    Task createTask(String title, String description, TaskStatus status);
    Task getTask(int id);
    ArrayList<Task> getAllTasks();
    void updateTask(Task task);
    void deleteTask(int id);
    void deleteAllTasks();

    Epic createEpic(String title, String description);
    Epic getEpic(int id);
    ArrayList<Epic> getAllEpics();
    void updateEpic(Epic epic);
    void deleteEpic(int id);
    void deleteAllEpics();

    Subtask createSubtask(String title, String description, TaskStatus status, int epicId);
    Subtask getSubtask(int id);
    ArrayList<Subtask> getAllSubtasks();
    void updateSubtask(Subtask subtask);
    void deleteSubtask(int id);
    void deleteAllSubtasks();

    ArrayList<Subtask> getEpicSubtasks(int epicId);

    ArrayList<Task> getHistory();
}
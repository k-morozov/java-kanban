package tracker.TaskManager;

import tracker.EventListener.EventListener;
import tracker.EventListener.HistoryEventListener;
import tracker.HistoryManager.HistoryManager;
import tracker.IssueRepo.*;
import tracker.Managers;
import tracker.issue.*;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager;
    private final EventListener listener;
    private final RepoManager repoManager;
    private int nextId;

    public InMemoryTaskManager() {
        historyManager = Managers.getDefaultHistory();

        listener = new HistoryEventListener(historyManager);
        Register register = new RepoRegister();

        register.register(Task.class, new TaskRepo(listener, new InMemoryPolicy<>()));
        register.register(Epic.class, new EpicRepo(listener, new InMemoryPolicy<>()));
        register.register(Subtask.class, new SubtaskRepo(listener, new InMemoryPolicy<>()));

        this.repoManager = new RepoManager(register);
        this.nextId = 1;
    }

    private int generateId() {
        return nextId++;
    }

    @Override
    public TaskView createTask(String title, String description, Status status) {
        Task task = new Task(title, description, generateId(), status);
        repoManager.save(Task.class, task);
        return new TaskView(task);
    }

    @Override
    public TaskView getTask(int id) {
        Task task = repoManager.get(Task.class, id);
        if (task == null) {
            return null;
        }
        return new TaskView(task);
    }

    @Override
    public List<TaskView> getAllTasks() {
        return repoManager.getAll(Task.class).stream().map(TaskView::new).toList();
    }

    @Override
    public void updateTask(Task task) {
        repoManager.update(Task.class, task);
    }

    @Override
    public void deleteTask(int id) {
        repoManager.remove(Task.class, id);
    }

    @Override
    public void deleteAllTasks() {
        repoManager.removeAll(Task.class);
    }

    @Override
    public EpicView createEpic(String title, String description) {
        Epic epic = new Epic(title, description, generateId());
        repoManager.save(Epic.class, epic);
        return new EpicView(epic);
    }

    @Override
    public EpicView getEpic(int id) {
        Epic epic = repoManager.get(Epic.class, id);
        if (epic == null) {
            return null;
        }
        return new EpicView(epic);
    }

    @Override
    public List<EpicView> getAllEpics() {
        return repoManager.getAll(Epic.class).stream().map(EpicView::new).toList();
    }

    @Override
    public void updateEpic(Epic epic) {
        repoManager.update(Epic.class, epic);
        updateEpicStatus(epic.getId());
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = repoManager.remove(Epic.class, id);
        if (epic == null) {
            throw new IllegalArgumentException("Epic not found");
        }
        ArrayList<Integer> copy = new ArrayList<>();
        copy.addAll(epic.getSubtaskIds());
        copy.forEach(subtaskId -> repoManager.remove(Subtask.class, subtaskId));
    }

    @Override
    public void deleteAllEpics() {
        repoManager.removeAll(Epic.class);
        repoManager.removeAll(Subtask.class);
    }

    @Override
    public SubtaskView createSubtask(String title, String description, Status status, int epicId) {
        Subtask subtask = new Subtask(title, description, generateId(), status, epicId);

        Epic epic = repoManager.get(Epic.class, subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Not found parent epic");
        }

        repoManager.save(Subtask.class, subtask);

        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(subtask.getEpicId());

        return new SubtaskView(subtask);
    }

    @Override
    public SubtaskView getSubtask(int id) {
        Subtask subtask = repoManager.get(Subtask.class, id);
        if (subtask == null) {
            return null;
        }
        return new SubtaskView(subtask);
    }

    @Override
    public List<SubtaskView> getAllSubtasks() {
        return repoManager.getAll(Subtask.class).stream().map(SubtaskView::new).toList();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        repoManager.update(Subtask.class, subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask subtask = repoManager.get(Subtask.class, subtaskId);
        int epicId = subtask.getEpicId();
        Epic epic = repoManager.get(Epic.class, epicId);
        epic.removeSubtaskId(subtaskId);

        repoManager.remove(Subtask.class, subtaskId);
    }

    @Override
    public void deleteAllSubtasks() {
        repoManager.removeAll(Subtask.class);
        repoManager.getAll(Epic.class).forEach(epic -> {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        });
    }

    @Override
    public List<SubtaskView> getEpicSubtasks(int epicId) {
        Epic epic = repoManager.get(Epic.class, epicId);
        if (epic == null) {
            return List.of();
        }

        ArrayList<SubtaskView> epicSubtasks = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = repoManager.get(Subtask.class, subtaskId);
            if (subtask != null) {
                epicSubtasks.add(new SubtaskView(subtask));
            }
        }
        return epicSubtasks;
    }

    @Override
    public List<ReadableIssue> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = repoManager.get(Epic.class, epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = repoManager.get(Subtask.class, subtaskId);
            if (subtask != null) {
                Status status = subtask.getStatus();
                if (status != Status.NEW) {
                    allNew = false;
                }
                if (status != Status.DONE) {
                    allDone = false;
                }
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
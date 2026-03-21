package tracker.TaskManager;

import tracker.EventListener.EventListener;
import tracker.EventListener.HistoryEventListener;
import tracker.HistoryManager.HistoryManager;
import tracker.IssueRepo.*;
import tracker.Managers;
import tracker.issue.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class AbstractTaskManager implements TaskManager {
    private final HistoryManager historyManager;
    private final EventListener listener;
    private final RepoManager repoManager;
    private int nextId;

    public AbstractTaskManager(PolicyFactory taskFactory, PolicyFactory epicFactory, PolicyFactory subtaskFactory) {
        historyManager = Managers.getDefaultHistory();

        listener = new HistoryEventListener(historyManager);
        Register register = new RepoRegister();

        register.register(Task.class, new TaskRepo(listener, taskFactory.create()));
        register.register(Epic.class, new EpicRepo(listener, epicFactory.create()));
        register.register(Subtask.class, new SubtaskRepo(listener, subtaskFactory.create()));

        this.repoManager = new RepoManager(register);
        this.nextId = 1;
    }

    private int generateId() {
        return nextId++;
    }

    private static boolean isConflictTime(ReadableIssue lhs, ReadableIssue rhs) {
        if (lhs.getId() == rhs.getId()) {
            return false;
        }
        LocalDateTime lhsStart = lhs.getStartTime().orElseThrow();
        LocalDateTime lhsEnd = lhs.getEndTime().orElseThrow();
        LocalDateTime rhsStart = rhs.getStartTime().orElseThrow();
        LocalDateTime rhsEnd = rhs.getEndTime().orElseThrow();

        return lhsStart.isBefore(rhsEnd) && rhsStart.isBefore(lhsEnd);
    }

    private void validate(ReadableIssue issue) {
        if (issue.getStartTime().isEmpty()) {
            return;
        }

        List<ReadableIssue> issues = getPrioritizedTasks();
        boolean r = issues.stream().anyMatch(candidate -> isConflictTime(issue, candidate));
        if (r) {
            throw new ConflictIssueTimeException("");
        }
    }

    protected void updateNextId(int candidate) {
        this.nextId = Integer.max(nextId, candidate);
    }

    @Override
    public TaskView createTask(String title, String description, Status status, LocalDateTime startTime, Duration duration) {
        return createTask(generateId(), title, description, status, startTime, duration);
    }

    protected TaskView createTask(int id, String title, String description, Status status, LocalDateTime startTime, Duration duration) {
        Task task = new Task(title, description, id, status, startTime, duration);
        return save(task);
    }

    protected TaskView save(Task task) {
        validate(task);
        repoManager.save(Task.class, task);
        return new TaskView(task);
    }

    @Override
    public TaskView getTask(int id) {
        Task task = repoManager.get(Task.class, id);
        if (task == null) {
            throw new NotFoundException("Task " + id + " not found");
        }
        return new TaskView(task);
    }

    @Override
    public List<TaskView> getAllTasks() {
        return repoManager.getAll(Task.class).stream().map(TaskView::new).toList();
    }

    @Override
    public void updateTask(Task task) {
        validate(task);
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
        return createEpic(generateId(), title, description);
    }

    protected EpicView createEpic(int id, String title, String description) {
        Epic epic = new Epic(title, description, id);
        return save(epic);
    }

    protected EpicView save(Epic epic) {
        repoManager.save(Epic.class, epic);
        return new EpicView(epic);
    }

    @Override
    public EpicView getEpic(int id) {
        Epic epic = repoManager.get(Epic.class, id);
        if (epic == null) {
            throw new NotFoundException("Epic " + id + " not found");
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
            throw new NotFoundException("Epic not found");
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
    public SubtaskView createSubtask(String title, String description, Status status,
                                     LocalDateTime startTime, Duration duration, int epicId) {
        return createSubtask(generateId(), title, description, status, startTime, duration, epicId);
    }

    protected SubtaskView createSubtask(int id, String title, String description, Status status,
                                        LocalDateTime startTime, Duration duration, int epicId) {
        Subtask subtask = new Subtask(title, description, id, status, startTime, duration, epicId);
        return save(subtask);
    }

    protected SubtaskView save(Subtask subtask) {
        Epic epic = repoManager.get(Epic.class, subtask.getEpicId());
        if (epic == null) {
            throw new NotFoundException("Not found parent epic");
        }

        validate(subtask);

        repoManager.save(Subtask.class, subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(subtask.getEpicId());

        updateEpicTime(epic);

        return new SubtaskView(subtask);
    }

    @Override
    public SubtaskView getSubtask(int id) {
        Subtask subtask = repoManager.get(Subtask.class, id);
        if (subtask == null) {
            throw new NotFoundException("Subtask " + id + " not found");
        }
        return new SubtaskView(subtask);
    }

    @Override
    public List<SubtaskView> getAllSubtasks() {
        return repoManager.getAll(Subtask.class).stream().map(SubtaskView::new).toList();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        validate(subtask);
        repoManager.update(Subtask.class, subtask);
        updateEpicStatus(subtask.getEpicId());

        Epic epic = repoManager.get(Epic.class, subtask.getEpicId());
        updateEpicTime(epic);
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask subtask = repoManager.get(Subtask.class, subtaskId);
        int epicId = subtask.getEpicId();
        Epic epic = repoManager.get(Epic.class, epicId);
        epic.removeSubtaskId(subtaskId);

        repoManager.remove(Subtask.class, subtaskId);

        updateEpicTime(epic);
    }

    @Override
    public void deleteAllSubtasks() {
        repoManager.removeAll(Subtask.class);
        repoManager.getAll(Epic.class).forEach(epic -> {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
            updateEpicTime(epic);
        });
    }

    @Override
    public List<SubtaskView> getEpicSubtasks(int epicId) {
        Epic epic = repoManager.get(Epic.class, epicId);
        if (epic == null) {
            return List.of();
        }

        return epic.getSubtaskIds().stream()
                .map(subtaskId -> repoManager.get(Subtask.class, subtaskId))
                .filter(Objects::nonNull)
                .map(SubtaskView::new)
                .toList();
    }

    @Override
    public List<ReadableIssue> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<ReadableIssue> getPrioritizedTasks() {
        List<ReadableIssue> tasks = getAllTasks().stream().map(task -> (ReadableIssue)task).toList();
        List<ReadableIssue> subtasks = getAllSubtasks().stream().map(subtask -> (ReadableIssue)subtask).toList();
        return MergeSorted.mergeSorted(tasks, subtasks);
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

    void updateEpicTime(Epic epic) {
        List<SubtaskView> sbs = getEpicSubtasks(epic.getId());
        if (sbs.isEmpty()) {
            epic.resetTime();
            return;
        }

        SubtaskView sb = sbs.getFirst();
        LocalDateTime start = sb.getStartTime().orElseThrow();
        Duration duration = sb.getDuration().orElseThrow();
        LocalDateTime end = sb.getEndTime().orElseThrow();

        for (int i = 1; i < sbs.size(); i++) {
            LocalDateTime candidateStart = sbs.get(i).getStartTime().orElseThrow();
            if (candidateStart.isBefore(start)) {
                start = candidateStart;
            }

            LocalDateTime candidateEnd = sbs.get(i).getEndTime().orElseThrow();
            if (candidateEnd.isAfter(end)) {
                end = candidateEnd;
            }

            duration = duration.plus(sbs.get(i).getDuration().orElseThrow());
        }

        epic.updateTime(start, duration, end);
    }
}

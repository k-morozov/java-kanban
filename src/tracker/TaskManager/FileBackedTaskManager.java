package tracker.TaskManager;

import tracker.IssueRepo.InMemoryPolicy;
import tracker.issue.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends AbstractTaskManager {
    private final Path path;
    private final Formatter formatter;

    private static final String schema = "id,type,name,status,description,epic";

    private class Loader {
        void load(String line) {
            String[] tokens = line.split(",");
            String type = tokens[1];

            switch (type) {
                case "TASK" -> {
                    Issue issue = FileBackedTaskManager.this.formatter.deserialize(tokens);
                    FileBackedTaskManager.this.updateNextId(issue.getId() + 1);
                    save((Task)issue);
                }
                case "EPIC" -> {
                    Issue issue = FileBackedTaskManager.this.formatter.deserialize(tokens);
                    FileBackedTaskManager.this.updateNextId(issue.getId() + 1);
                    save((Epic)issue);
                }
                case "SUBTASK" -> {
                    Issue issue = FileBackedTaskManager.this.formatter.deserialize(tokens);
                    FileBackedTaskManager.this.updateNextId(issue.getId() + 1);
                    save((Subtask)issue);
                }
                default -> throw new IllegalArgumentException("Type " + type + " is not supported");
            }
        }
    }

    public FileBackedTaskManager(Path path) {
        super(InMemoryPolicy::create);
        this.path = path;
        this.formatter = new CSVFormatter();

        loadFromFile();
    }

    private void loadFromFile() {
        Loader loader = new Loader();
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
                dump();
            } else {
                String lines = Files.readString(path);

                String[] split = lines.split("\n");
                for (int i = 1; i < split.length; i++) {
                    loader.load(split[i]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dump() {
        try {
            List<String> lines = new ArrayList<>();

            lines.add(schema);

            getAllTasks().stream()
                    .map(tv -> tv.serialize(formatter))
                    .forEach(lines::add);

            Files.write(path, lines, StandardCharsets.UTF_8);

            getAllEpics().stream()
                    .map(ev -> ev.serialize(formatter))
                    .forEach(lines::add);

            Files.write(path, lines, StandardCharsets.UTF_8);

            getAllSubtasks().stream()
                    .map(sv -> sv.serialize(formatter))
                    .forEach(lines::add);

            Files.write(path, lines, StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new ManagerSaveException("Can't save to file: " + path, e);
        }
    }

    @Override
    public TaskView createTask(String title, String description, Status status) {
        TaskView res = super.createTask(title, description, status);
        dump();
        return res;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        dump();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        dump();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        dump();
    }

    @Override
    public EpicView createEpic(String title, String description) {
        EpicView res = super.createEpic(title, description);
        dump();
        return res;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        dump();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        dump();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        dump();
    }

    @Override
    public SubtaskView createSubtask(String title, String description, Status status, int epicId) {
        SubtaskView res = super.createSubtask(title, description, status, epicId);
        dump();
        return res;
    }


    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        dump();
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        super.deleteSubtask(subtaskId);
        dump();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        dump();
    }

    static void main() {
        TaskManager tm = new FileBackedTaskManager(Path.of("tasks.csv"));

        tm.createTask("task3", "description", Status.NEW);
        tm.createTask("task2", "description", Status.IN_PROGRESS);

        TaskView view = tm.getTask(2);
        System.out.println(view);
    }
}

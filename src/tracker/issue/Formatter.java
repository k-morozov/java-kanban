package tracker.issue;

public interface Formatter {
    String serialize(Task task);

    Issue deserialize(String[] tokens);

    String serialize(Epic task);

    String serialize(Subtask task);
}

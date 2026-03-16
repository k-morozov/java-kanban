package tracker.issue;

import java.time.Duration;
import java.time.LocalDateTime;

public class CSVFormatter implements Formatter {

    @Override
    public String serialize(Task task) {
        StringBuilder builder = new StringBuilder();

        builder.append(task.getId());
        builder.append(",TASK");

        serializeIssue(builder, task);

        return builder.toString();
    }

    @Override
    public Issue deserialize(String[] tokens) {
        // we have the guarantee valid input
        String id = tokens[0];
        String type = tokens[1];
        String title = tokens[2];
        String status = tokens[3];
        String description = tokens[4];
        if ("TASK".equals(type)) {
            if (tokens.length > 5) {
                String startTime = tokens[5];
                String duration = tokens[6];

                return new Task(title, description, Integer.parseInt(id), Status.valueOf(status),
                        LocalDateTime.parse(startTime), Duration.parse(duration));
            }
            return new Task(title, description, Integer.parseInt(id), Status.valueOf(status));
        }
        if ("EPIC".equals(type)) {
            return new Epic(title, description, Integer.parseInt(id));
        }
        String startTime = tokens[5];
        String duration = tokens[6];
        String epicId = tokens[7];
        return new Subtask(title, description, Integer.parseInt(id), Status.valueOf(status),
                LocalDateTime.parse(startTime), Duration.parse(duration), Integer.parseInt(epicId));
    }

    @Override
    public String serialize(Epic epic) {
        StringBuilder builder = new StringBuilder();

        builder.append(epic.getId());
        builder.append(",EPIC");

        serializeIssue(builder, epic);

        return builder.toString();
    }


    @Override
    public String serialize(Subtask subtask) {
        StringBuilder builder = new StringBuilder();

        builder.append(subtask.getId());
        builder.append(",SUBTASK");

        serializeIssue(builder, subtask);

        builder.append(",").append(subtask.getEpicId());

        return builder.toString();
    }

    static void serializeIssue(StringBuilder builder, ReadableIssue issue) {
        builder.append(",").append(issue.getTitle());
        builder.append(",").append(issue.getStatus());
        builder.append(",").append(issue.getDescription());

        if (issue.getStartTime().isPresent()) {
            builder.append(",").append(issue.getStartTime().get());
        }

        if (issue.getDuration().isPresent()) {
            builder.append(",").append(issue.getDuration().get());
        }
    }
}

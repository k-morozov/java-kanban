package tracker.issue;

public class CSVFormatter implements Formatter {

    @Override
    public String serialize(Task task) {
        return task.getId() + ",TASK," + task.getTitle() + "," + task.getStatus() + ',' + task.getDescription();
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
            return new Task(title, description, Integer.parseInt(id), Status.valueOf(status));
        }
        if ("EPIC".equals(type)) {
            return new Epic(title, description, Integer.parseInt(id));
        }
        String epicId = tokens[5];
        return new Subtask(title, description, Integer.parseInt(id), Status.valueOf(status), Integer.parseInt(epicId));
    }

    @Override
    public String serialize(Epic epic) {
        return epic.getId() + ",EPIC," + epic.getTitle() + "," + epic.getStatus() + ',' + epic.getDescription();
    }


    @Override
    public String serialize(Subtask subtask) {
        return subtask.getId() + ",SUBTASK," + subtask.getTitle() + "," + subtask.getStatus() + ','
                + subtask.getDescription() + ',' + subtask.getEpicId();
    }
}

package tracker.issue;

public class TaskView extends IssueView<Task> implements Serializer  {
    public TaskView(Task task) {
        super(task);
    }

    @Override
    public String serialize(Formatter formatter) {
        return formatter.serialize(getIssue());
    }
}

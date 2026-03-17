package tracker.Server.Input;

public class EpicUpdateInput {
    public String title;
    public String description;

    public void validate() {
        if (title == null || title.isEmpty()) {
            throw new InputValidationException("Title must be non empty");
        }
    }
}

package tracker.Server;

import com.sun.net.httpserver.HttpExchange;
import tracker.Server.Input.InputValidationException;
import tracker.Server.Input.TaskCreateInput;
import tracker.Server.Input.TaskUpdateInput;
import tracker.Server.Output.TaskCreateOutput;
import tracker.Server.Output.TaskGetOutput;
import tracker.TaskManager.ConflictIssueTimeException;
import tracker.TaskManager.NotFoundException;
import tracker.TaskManager.TaskManager;
import tracker.issue.Task;
import tracker.issue.TaskView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

class TasksHandler extends BaseHttpHandler {
    protected static String path = "/tasks";

    TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (httpExchange) {
            try {
                String method = httpExchange.getRequestMethod();

                switch (HttpMethods.valueOf(method)) {
                    case HttpMethods.POST -> handlePost(httpExchange);
                    case HttpMethods.GET -> handleGet(httpExchange);
                    case HttpMethods.DELETE -> handleDelete(httpExchange);
                }
            } catch (Exception e) {
                sendServerInternalError(httpExchange);
            }
        }
    }

    private void handlePost(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();

        String path = requestURI.getPath();
        String[] splitStrings = path.split("/");

        if (splitStrings.length == 2) {
            doCreateTask(httpExchange);
        } else {
            String taskId = splitStrings[2];
            doUpdateTask(httpExchange, taskId);
        }
    }

    private void handleGet(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();

        String path = requestURI.getPath();
        String[] splitStrings = path.split("/");

        if (splitStrings.length == 2) {
            doGetAllTasks(httpExchange);
        } else {
            String taskId = splitStrings[2];
            doGetTask(httpExchange, taskId);
        }
    }

    private void handleDelete(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();

        String path = requestURI.getPath();
        String[] splitStrings = path.split("/");

        if (splitStrings.length != 3) {
            throw new InputValidationException("Broken request");
        }
        String taskId = splitStrings[2];
        doDeleteTask(httpExchange, taskId);
    }

    private void doCreateTask(HttpExchange httpExchange) throws IOException  {
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        TaskCreateInput input = gson.fromJson(body, TaskCreateInput.class);
        input.validate();

        TaskView view;
        try {
            view = manager.createTask(input.title, input.description, input.status,
                    LocalDateTime.parse(input.startTime), Duration.parse(input.duration));
        } catch (ConflictIssueTimeException ex) {
            sendHasInteractions(httpExchange);
            return;
        }

        TaskCreateOutput output = new TaskCreateOutput();
        output.taskId = view.getId();

        sendText(httpExchange, HttpStatusCode.CREATED, gson.toJson(output));
    }

    private void doGetAllTasks(HttpExchange httpExchange) throws IOException {
        List<TaskView> views = manager.getAllTasks();
        List<TaskGetOutput> output = views.stream()
                .map(TaskGetOutput::from)
                .toList();

        sendText(httpExchange, HttpStatusCode.OK, gson.toJson(output));
    }

    private void doGetTask(HttpExchange httpExchange, String taskId) throws IOException {
        TaskView view;
        try {
            view = manager.getTask(Integer.parseInt(taskId));
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        TaskGetOutput output = TaskGetOutput.from(view);
        sendText(httpExchange, HttpStatusCode.OK, gson.toJson(output));
    }

    private void doUpdateTask(HttpExchange httpExchange, String taskId) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        TaskUpdateInput input = gson.fromJson(body, TaskUpdateInput.class);
        input.validate();

        try {
            manager.updateTask(new Task(input.title, input.description, Integer.parseInt(taskId), input.status,
                    LocalDateTime.parse(input.startTime), Duration.parse(input.duration)));
        } catch (ConflictIssueTimeException ex) {
            sendHasInteractions(httpExchange);
            return;
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        sendText(httpExchange, HttpStatusCode.CREATED, gson.toJson(""));
    }

    private void doDeleteTask(HttpExchange httpExchange, String taskId) throws IOException {
        try {
            manager.deleteTask(Integer.parseInt(taskId));
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        sendText(httpExchange, HttpStatusCode.OK, gson.toJson(""));
    }
}

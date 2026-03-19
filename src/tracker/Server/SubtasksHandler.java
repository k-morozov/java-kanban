package tracker.Server;

import com.sun.net.httpserver.HttpExchange;
import tracker.Server.Input.InputValidationException;
import tracker.Server.Input.SubtaskCreateInput;
import tracker.Server.Input.SubtaskUpdateInput;
import tracker.Server.Output.TaskCreateOutput;
import tracker.Server.Output.SubtaskGetOutput;
import tracker.TaskManager.ConflictIssueTimeException;
import tracker.TaskManager.NotFoundException;
import tracker.TaskManager.TaskManager;
import tracker.issue.Subtask;
import tracker.issue.SubtaskView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

class SubtasksHandler extends BaseHttpHandler {
    protected static String path = "/subtasks";

    SubtasksHandler(TaskManager manager) {
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
            doCreateSubtask(httpExchange);
        } else {
            String subtaskId = splitStrings[2];
            doUpdateSubtask(httpExchange, subtaskId);
        }
    }

    private void handleGet(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();

        String path = requestURI.getPath();
        String[] splitStrings = path.split("/");

        if (splitStrings.length == 2) {
            doGetAllSubtasks(httpExchange);
        } else {
            String subtaskId = splitStrings[2];
            doGetSubtask(httpExchange, subtaskId);
        }
    }

    private void handleDelete(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();

        String path = requestURI.getPath();
        String[] splitStrings = path.split("/");

        if (splitStrings.length != 3) {
            throw new InputValidationException("Broken request");
        }
        String subtaskId = splitStrings[2];
        doDeleteSubtask(httpExchange, subtaskId);
    }

    private void doCreateSubtask(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        SubtaskCreateInput input = gson.fromJson(body, SubtaskCreateInput.class);
        input.validate();

        SubtaskView view;
        try {
            view = manager.createSubtask(
                    input.title,
                    input.description,
                    input.status,
                    LocalDateTime.parse(input.startTime),
                    Duration.parse(input.duration),
                    input.epicId
            );
        } catch (ConflictIssueTimeException ex) {
            sendHasInteractions(httpExchange);
            return;
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        TaskCreateOutput output = new TaskCreateOutput();
        output.taskId = view.getId();

        sendText(httpExchange, HttpStatusCode.CREATED, gson.toJson(output));
    }

    private void doGetAllSubtasks(HttpExchange httpExchange) throws IOException {
        List<SubtaskView> views = manager.getAllSubtasks();
        List<SubtaskGetOutput> output = views.stream()
                .map(SubtaskGetOutput::from)
                .toList();

        sendText(httpExchange, HttpStatusCode.OK, gson.toJson(output));
    }

    private void doGetSubtask(HttpExchange httpExchange, String subtaskId) throws IOException {
        SubtaskView view;
        try {
            view = manager.getSubtask(Integer.parseInt(subtaskId));
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        SubtaskGetOutput output = SubtaskGetOutput.from(view);
        sendText(httpExchange, HttpStatusCode.OK, gson.toJson(output));
    }

    private void doUpdateSubtask(HttpExchange httpExchange, String subtaskId) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        SubtaskUpdateInput input = gson.fromJson(body, SubtaskUpdateInput.class);
        input.validate();

        try {
            manager.updateSubtask(new Subtask(
                    input.title,
                    input.description,
                    Integer.parseInt(subtaskId),
                    input.status,
                    LocalDateTime.parse(input.startTime),
                    Duration.parse(input.duration),
                    input.epicId
            ));
        } catch (ConflictIssueTimeException ex) {
            sendHasInteractions(httpExchange);
            return;
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        sendText(httpExchange, HttpStatusCode.CREATED, gson.toJson(""));
    }

    private void doDeleteSubtask(HttpExchange httpExchange, String subtaskId) throws IOException {
        try {
            manager.deleteSubtask(Integer.parseInt(subtaskId));
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        sendText(httpExchange, HttpStatusCode.OK, gson.toJson(""));
    }
}

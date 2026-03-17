package tracker.Server;

import com.sun.net.httpserver.HttpExchange;
import tracker.Server.Input.EpicCreateInput;
import tracker.Server.Input.EpicUpdateInput;
import tracker.Server.Input.InputValidationException;
import tracker.Server.Output.EpicCreateOutput;
import tracker.Server.Output.EpicGetOutput;
import tracker.Server.Output.SubtaskGetOutput;
import tracker.TaskManager.NotFoundException;
import tracker.TaskManager.TaskManager;
import tracker.issue.Epic;
import tracker.issue.EpicView;
import tracker.issue.SubtaskView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

class EpicsHandler extends BaseHttpHandler {
    protected static String path = "/epics";

    EpicsHandler(TaskManager manager) {
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
        String[] splitStrings = requestURI.getPath().split("/");

        if (splitStrings.length == 2) {
            doCreateEpic(httpExchange);
        } else {
            doUpdateEpic(httpExchange, splitStrings[2]);
        }
    }

    private void handleGet(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();
        String[] splitStrings = requestURI.getPath().split("/");

        if (splitStrings.length == 2) {
            doGetAllEpics(httpExchange);
        } else if (splitStrings.length == 3) {
            doGetEpic(httpExchange, splitStrings[2]);
        } else if (splitStrings.length == 4 && "subtasks".equals(splitStrings[3])) {
            doGetEpicSubtasks(httpExchange, splitStrings[2]);
        } else {
            throw new InputValidationException("Broken request");
        }
    }

    private void handleDelete(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();
        String[] splitStrings = requestURI.getPath().split("/");

        if (splitStrings.length != 3) {
            throw new InputValidationException("Broken request");
        }

        doDeleteEpic(httpExchange, splitStrings[2]);
    }

    private void doCreateEpic(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        EpicCreateInput input = gson.fromJson(body, EpicCreateInput.class);
        input.validate();

        EpicView view = manager.createEpic(input.title, input.description);

        EpicCreateOutput output = new EpicCreateOutput();
        output.epicId = view.getId();

        sendText(httpExchange, 201, gson.toJson(output));
    }

    private void doGetAllEpics(HttpExchange httpExchange) throws IOException {
        List<EpicView> views = manager.getAllEpics();
        List<EpicGetOutput> output = views.stream()
                .map(EpicGetOutput::from)
                .toList();

        sendText(httpExchange, 200, gson.toJson(output));
    }

    private void doGetEpic(HttpExchange httpExchange, String epicId) throws IOException {
        EpicView view;
        try {
            view = manager.getEpic(Integer.parseInt(epicId));
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        EpicGetOutput output = EpicGetOutput.from(view);
        sendText(httpExchange, 200, gson.toJson(output));
    }

    private void doUpdateEpic(HttpExchange httpExchange, String epicId) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        EpicUpdateInput input = gson.fromJson(body, EpicUpdateInput.class);
        input.validate();

        try {
            manager.updateEpic(new Epic(input.title, input.description, Integer.parseInt(epicId)));
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        sendText(httpExchange, 201, gson.toJson(""));
    }

    private void doDeleteEpic(HttpExchange httpExchange, String epicId) throws IOException {
        try {
            manager.deleteEpic(Integer.parseInt(epicId));
        } catch (NotFoundException ex) {
            sendNotFound(httpExchange);
            return;
        }

        sendText(httpExchange, 200, gson.toJson(""));
    }

    private void doGetEpicSubtasks(HttpExchange httpExchange, String epicId) throws IOException {
        List<SubtaskView> views = manager.getEpicSubtasks(Integer.parseInt(epicId));

        List<SubtaskGetOutput> output = views.stream()
                .map(SubtaskGetOutput::from)
                .toList();

        sendText(httpExchange, 200, gson.toJson(output));
    }
}

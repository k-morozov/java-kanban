package tracker.Server;

import com.sun.net.httpserver.HttpExchange;
import tracker.Server.Output.HistoryGetOutput;
import tracker.TaskManager.TaskManager;
import tracker.issue.ReadableIssue;

import java.io.IOException;
import java.net.URI;
import java.util.List;

class HistoryHandler extends BaseHttpHandler {
    protected static String path = "/history";

    HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (httpExchange) {
            try {
                String method = httpExchange.getRequestMethod();

                switch (HttpMethods.valueOf(method)) {
                    case HttpMethods.GET -> handleGet(httpExchange);
                    default -> sendServerInternalError(httpExchange);
                }
            } catch (Exception e) {
                sendServerInternalError(httpExchange);
            }
        }
    }

    private void handleGet(HttpExchange httpExchange) throws IOException {
        URI requestURI = httpExchange.getRequestURI();
        String[] splitStrings = requestURI.getPath().split("/");

        if (splitStrings.length != 2) {
            throw new IllegalArgumentException("Broken request");
        }

        doGetHistory(httpExchange);
    }

    private void doGetHistory(HttpExchange httpExchange) throws IOException {
        List<ReadableIssue> history = manager.getHistory();
        List<HistoryGetOutput> output = history.stream()
                .map(HistoryGetOutput::from)
                .toList();

        sendText(httpExchange, HttpStatusCode.OK, gson.toJson(output));
    }
}

package tracker.Server;

import com.sun.net.httpserver.HttpExchange;
import tracker.Server.Output.PrioritizedGetOutput;
import tracker.TaskManager.TaskManager;
import tracker.issue.ReadableIssue;

import java.io.IOException;
import java.util.List;

class PrioritizedHandler extends BaseHttpHandler {
    protected static String path = "/prioritized";

    PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (httpExchange) {
            try {
                String method = httpExchange.getRequestMethod();

                switch (HttpMethods.valueOf(method)) {
                    case HttpMethods.GET -> doGetPrioritized(httpExchange);
                    default -> sendServerInternalError(httpExchange);
                }
            } catch (Exception e) {
                sendServerInternalError(httpExchange);
            }
        }
    }

    private void doGetPrioritized(HttpExchange httpExchange) throws IOException {
        List<ReadableIssue> prioritized = manager.getPrioritizedTasks();
        List<PrioritizedGetOutput> output = prioritized.stream()
                .map(PrioritizedGetOutput::from)
                .toList();

        sendText(httpExchange, HttpStatusCode.OK, gson.toJson(output));
    }
}

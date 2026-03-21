package tracker.Server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.TaskManager.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected TaskManager manager;
    protected final Gson gson = new Gson();

    BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    protected void sendText(HttpExchange httpExchange, HttpStatusCode code, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(code.getCode(), resp.length);

        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(resp);
        }
    }

    protected void sendNotFound(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpStatusCode.NOT_FOUND.getCode(), 0);
    }

    protected void sendHasInteractions(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpStatusCode.NOT_ACCEPTABLE.getCode(), 0);
    }

    protected void sendServerInternalError(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), 0);
    }
}

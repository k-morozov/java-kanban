package tracker.Server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tracker.TaskManager.InMemoryTaskManager;
import tracker.TaskManager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer httpServer;
    private final TaskManager manager;
    private final Map<String, HttpHandler> handlers;

    private HttpTaskServer() {
        manager = new InMemoryTaskManager();
        handlers = Map.of(
                TasksHandler.path, new TasksHandler(manager),
                SubtasksHandler.path, new SubtasksHandler(manager),
                EpicsHandler.path, new EpicsHandler(manager),
                HistoryHandler.path, new HistoryHandler(manager),
                PrioritizedHandler.path, new PrioritizedHandler(manager)
        );

        try {
            httpServer = HttpServer.create();
            httpServer.bind(new InetSocketAddress(PORT), 5);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        handlers.forEach(httpServer::createContext);
    }

    static HttpTaskServer create() {
        return new HttpTaskServer();
    }

    void start() {
        httpServer.start();
    }

    void stop() {
        httpServer.stop(1);
    }

    static void main() {
        HttpTaskServer httpServer = HttpTaskServer.create();
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }
}
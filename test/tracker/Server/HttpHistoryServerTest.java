package tracker.Server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.Server.Output.EpicCreateOutput;
import tracker.Server.Output.HistoryGetOutput;
import tracker.Server.Output.TaskCreateOutput;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpHistoryServerTest {
    private final HttpTaskServer server = HttpTaskServer.create();
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void getHistory() throws IOException, InterruptedException {
        int taskId = createTask();
        int epicId = createEpic();

        HttpRequest getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> taskResponse = client.send(getTaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, taskResponse.statusCode());

        HttpRequest getEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> epicResponse = client.send(getEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, epicResponse.statusCode());

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());

        HistoryGetOutput[] history = gson.fromJson(historyResponse.body(), HistoryGetOutput[].class);
        assertNotNull(history);
        assertEquals(2, history.length);
        assertEquals(taskId, history[0].id);
        assertEquals(epicId, history[1].id);
    }

    @Test
    void getEmptyHistory() throws IOException, InterruptedException {
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());
        assertEquals("[]", historyResponse.body());
    }

    @Test
    void historyShouldRemoveDeletedItems() throws IOException, InterruptedException {
        int taskId = createTask();
        int epicId = createEpic();

        readTask(taskId);
        readEpic(epicId);

        HttpRequest deleteTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> deleteTaskResponse = client.send(deleteTaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteTaskResponse.statusCode());

        HttpRequest deleteEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> deleteEpicResponse = client.send(deleteEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteEpicResponse.statusCode());

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());

        HistoryGetOutput[] history = gson.fromJson(historyResponse.body(), HistoryGetOutput[].class);
        assertNotNull(history);
        assertEquals(0, history.length);
    }

    @Test
    void historyShouldMoveRepeatedAccessToEnd() throws IOException, InterruptedException {
        int taskId = createTask();
        int epicId = createEpic();

        readTask(taskId);
        readEpic(epicId);
        readTask(taskId);

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());

        HistoryGetOutput[] history = gson.fromJson(historyResponse.body(), HistoryGetOutput[].class);
        assertNotNull(history);
        assertEquals(2, history.length);
        assertEquals(epicId, history[0].id);
        assertEquals(taskId, history[1].id);
    }

    private int createTask() throws IOException, InterruptedException {
        String body = """
            {
                "title": "task1",
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        TaskCreateOutput output = gson.fromJson(response.body(), TaskCreateOutput.class);
        return output.taskId;
    }

    private int createEpic() throws IOException, InterruptedException {
        String body = """
            {
                "title": "epic1",
                "description": "description1"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        EpicCreateOutput output = gson.fromJson(response.body(), EpicCreateOutput.class);
        return output.epicId;
    }

    private void readTask(int taskId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    private void readEpic(int epicId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }
}
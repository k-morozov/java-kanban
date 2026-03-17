package tracker.Server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.Server.Output.EpicCreateOutput;
import tracker.Server.Output.PrioritizedGetOutput;
import tracker.Server.Output.TaskCreateOutput;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpPrioritizedServerTest {
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
    void getPrioritizedEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void getPrioritizedTasksAndSubtasks() throws IOException, InterruptedException {
        int epicId = createEpic();

        int taskId1 = createTask("task1", "2026-03-17T10:15:30");
        int taskId2 = createTask("task2", "2026-03-17T10:15:50");

        int subtaskId = createSubtask(epicId, "subtask1", "2026-03-17T10:15:40");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        PrioritizedGetOutput[] output = gson.fromJson(response.body(), PrioritizedGetOutput[].class);
        assertNotNull(output);
        assertEquals(3, output.length);

        assertEquals(taskId1, output[0].id);
        assertEquals(subtaskId, output[1].id);
        assertEquals(taskId2, output[2].id);
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

    private int createTask(String title, String startTime) throws IOException, InterruptedException {
        String body = """
            {
                "title": "%s",
                "description": "description1",
                "status": "NEW",
                "startTime": "%s",
                "duration": "PT10S"
            }
            """.formatted(title, startTime);

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

    private int createSubtask(int epicId, String title, String startTime) throws IOException, InterruptedException {
        String body = """
            {
                "title": "%s",
                "description": "description1",
                "status": "NEW",
                "epicId": %d,
                "startTime": "%s",
                "duration": "PT10S"
            }
            """.formatted(title, epicId, startTime);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        TaskCreateOutput output = gson.fromJson(response.body(), TaskCreateOutput.class);
        return output.taskId;
    }
}

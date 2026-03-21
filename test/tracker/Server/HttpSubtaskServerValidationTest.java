package tracker.Server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.Server.Output.TaskCreateOutput;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpSubtaskServerValidationTest {
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
    void createSubtask() throws IOException, InterruptedException {
        String epicBody = """
            {
                "title": "epic1",
                "description": "description1"
            }
            """;

        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode());

        String body = """
            {
                "title": "subtask1",
                "description": "description1",
                "status": "NEW",
                "epicId": 1,
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        TaskCreateOutput output = gson.fromJson(response.body(), TaskCreateOutput.class);
        assertNotNull(output);
    }

    @Test
    void failedCreateSubtaskUnknownEpic() throws IOException, InterruptedException {
        String body = """
            {
                "title": "subtask1",
                "description": "description1",
                "status": "NEW",
                "epicId": 999,
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void failedCreateSubtaskNoTitle() throws IOException, InterruptedException {
        String body = """
            {
                "description": "description1",
                "status": "NEW",
                "epicId": 1,
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode());
    }
}
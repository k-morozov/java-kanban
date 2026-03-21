package tracker.Server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.Server.Output.EpicCreateOutput;
import tracker.Server.Output.SubtaskGetOutput;
import tracker.Server.Output.TaskCreateOutput;
import tracker.issue.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpSubtaskServerTest {
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
    void getCreatedSubtask() throws IOException, InterruptedException {
        int epicId = createEpic();

        String body1 = """
            {
                "title": "subtask1",
                "description": "description1",
                "status": "NEW",
                "epicId": %d,
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """.formatted(epicId);

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        TaskCreateOutput output = gson.fromJson(response.body(), TaskCreateOutput.class);

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + output.taskId))
                .GET()
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode());

        SubtaskGetOutput output2 = gson.fromJson(response2.body(), SubtaskGetOutput.class);
        assertEquals("subtask1", output2.title);
        assertEquals(1, output2.epicId);
        assertEquals(Status.NEW, output2.status);
    }

    @Test
    void getEpicSubtasks() throws IOException, InterruptedException {
        int epicId = createEpic();
        createSubtask(epicId, "subtask1", "2026-03-17T10:15:30");
        createSubtask(epicId, "subtask2", "2026-03-17T10:15:40");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        SubtaskGetOutput[] output = gson.fromJson(response.body(), SubtaskGetOutput[].class);
        assertEquals(2, output.length);
    }

    @Test
    void getNotFoundSubtask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/111"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    private int createEpic() throws IOException, InterruptedException {
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

        EpicCreateOutput epicOutput = gson.fromJson(epicResponse.body(), EpicCreateOutput.class);
        return epicOutput.epicId;
    }

    private void createSubtask(int epicId, String title, String startTime) throws IOException, InterruptedException {
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
    }
}

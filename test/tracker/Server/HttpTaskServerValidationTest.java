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

class HttpTaskServerValidationTest {
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
    void createTask() throws IOException, InterruptedException {
        String body = """
            {
                "title": "title1",
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks")) // проверьте ваш порт
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        body = response.body();
        assertNotNull(body);

         TaskCreateOutput output = gson.fromJson(body, TaskCreateOutput.class);
         assertNotNull(output);
    }

    @Test
    void FailedCreateTaskNoTitle() throws IOException, InterruptedException {
        String body = """
            {
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks")) // проверьте ваш порт
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
    }

    @Test
    void FailedCreateTaskEmptyTitle() throws IOException, InterruptedException {
        String body = """
            {
                "title": "",
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks")) // проверьте ваш порт
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
    }

    @Test
    void FailedCreateTaskNoStatus() throws IOException, InterruptedException {
        String body = """
            {
                "title": "title1",
                "description": "description1",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks")) // проверьте ваш порт
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
    }

    @Test
    void FailedCreateTaskUnknownStatus() throws IOException, InterruptedException {
        String body = """
            {
                "title": "title1",
                "status": "NOTNEW",
                "description": "description1",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks")) // проверьте ваш порт
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
    }
}
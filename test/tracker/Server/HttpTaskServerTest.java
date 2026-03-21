package tracker.Server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.Server.Output.TaskCreateOutput;
import tracker.Server.Output.TaskGetOutput;
import tracker.issue.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpTaskServerTest {
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
    void tasksTimeConflict() throws IOException, InterruptedException {
        String body1 = """
            {
                "title": "title1",
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks")) // проверьте ваш порт
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        String body2 = """
            {
                "title": "title2",
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:25",
                "duration": "PT10S"
            }
            """;

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks")) // проверьте ваш порт
                .POST(HttpRequest.BodyPublishers.ofString(body2))
                .header("Content-Type", "application/json")
                .build();

        response = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    void getCreatedTask() throws IOException, InterruptedException {
        String body1 = """
            {
                "title": "title1",
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        TaskCreateOutput output = gson.fromJson(response.body(), TaskCreateOutput.class);

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + output.taskId))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode());

        TaskGetOutput output2 = gson.fromJson(response2.body(), TaskGetOutput.class);
        assertEquals("title1", output2.title);
        assertEquals("description1", output2.description);
        assertEquals(Status.NEW, output2.status);
        assertEquals("2026-03-17T10:15:30", output2.startTime);
        assertEquals("PT10S", output2.duration);
    }

    @Test
    void getNotFoundTask() throws IOException, InterruptedException {

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + 111))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response2.statusCode());
    }

    @Test
    void getAllCreatedTasks() throws IOException, InterruptedException {
        String body1 = """
            {
                "title": "title1",
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:30",
                "duration": "PT10S"
            }
            """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        String body2 = """
            {
                "title": "title2",
                "description": "description1",
                "status": "NEW",
                "startTime": "2026-03-17T10:15:40",
                "duration": "PT10S"
            }
            """;

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body2))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode());

        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response3.statusCode());

        TaskGetOutput[] output = gson.fromJson(response3.body(), TaskGetOutput[].class);
        assertEquals(2, output.length);

        assertEquals("title1", output[0].title);
        assertEquals("description1", output[0].description);
        assertEquals(Status.NEW, output[0].status);
        assertEquals("2026-03-17T10:15:30", output[0].startTime);
        assertEquals("PT10S", output[0].duration);

        assertEquals("title2", output[1].title);
        assertEquals("description1", output[1].description);
        assertEquals(Status.NEW, output[1].status);
        assertEquals("2026-03-17T10:15:40", output[1].startTime);
        assertEquals("PT10S", output[1].duration);
    }

    @Test
    void updateNotFoundTask() throws IOException, InterruptedException {
        String body1 = """
                {
                    "title": "title1",
                    "description": "description1",
                    "status": "NEW",
                    "startTime": "2026-03-17T10:15:30",
                    "duration": "PT10S"
                }
                """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        TaskCreateOutput output = gson.fromJson(response.body(), TaskCreateOutput.class);

        String body2 = """
                {
                    "title": "title2",
                    "description": "description2",
                    "status": "DONE",
                    "startTime": "2026-03-17T10:15:40",
                    "duration": "PT11S"
                }
                """;
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + 111))
                .POST(HttpRequest.BodyPublishers.ofString(body2))
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response2.statusCode());
    }

    @Test
    void updateTask() throws IOException, InterruptedException {
        String body1 = """
                {
                    "title": "title1",
                    "description": "description1",
                    "status": "NEW",
                    "startTime": "2026-03-17T10:15:30",
                    "duration": "PT10S"
                }
                """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        TaskCreateOutput output = gson.fromJson(response.body(), TaskCreateOutput.class);

        String body2 = """
                {
                    "title": "title2",
                    "description": "description2",
                    "status": "DONE",
                    "startTime": "2026-03-17T10:15:31",
                    "duration": "PT11S"
                }
                """;
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + output.taskId))
                .POST(HttpRequest.BodyPublishers.ofString(body2))
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode());

        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + output.taskId))
                .GET()
                .build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response3.statusCode());

        TaskGetOutput output2 = gson.fromJson(response3.body(), TaskGetOutput.class);
        assertEquals("title2", output2.title);
        assertEquals("description2", output2.description);
        assertEquals(Status.DONE, output2.status);
        assertEquals("2026-03-17T10:15:31", output2.startTime);
        assertEquals("PT11S", output2.duration);
    }

    @Test
    void deleteTask() throws IOException, InterruptedException {
        String body1 = """
                {
                    "title": "title1",
                    "description": "description1",
                    "status": "NEW",
                    "startTime": "2026-03-17T10:15:30",
                    "duration": "PT10S"
                }
                """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        TaskCreateOutput output = gson.fromJson(response.body(), TaskCreateOutput.class);

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + output.taskId))
                .GET()
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode());

        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/"  + output.taskId))
                .DELETE()
                .build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response3.statusCode());

        HttpRequest request4 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + output.taskId))
                .GET()
                .build();

        HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response4.statusCode());
    }
}
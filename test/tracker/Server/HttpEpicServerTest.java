package tracker.Server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.Server.Output.EpicCreateOutput;
import tracker.Server.Output.EpicGetOutput;
import tracker.issue.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpEpicServerTest {
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
    void getCreatedEpic() throws IOException, InterruptedException {
        String body1 = """
            {
                "title": "epic1",
                "description": "description1"
            }
            """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        EpicCreateOutput output = gson.fromJson(response.body(), EpicCreateOutput.class);

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + output.epicId))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode());

        EpicGetOutput output2 = gson.fromJson(response2.body(), EpicGetOutput.class);
        assertEquals("epic1", output2.title);
        assertEquals("description1", output2.description);
        assertEquals(Status.NEW, output2.status);
    }

    @Test
    void getNotFoundEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/111"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void getAllCreatedEpics() throws IOException, InterruptedException {
        String body1 = """
            {
                "title": "epic1",
                "description": "description1"
            }
            """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        String body2 = """
            {
                "title": "epic2",
                "description": "description2"
            }
            """;

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(body2))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode());

        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response3.statusCode());

        EpicGetOutput[] output = gson.fromJson(response3.body(), EpicGetOutput[].class);
        assertEquals(2, output.length);
        assertEquals("epic1", output[0].title);
        assertEquals("epic2", output[1].title);
    }

    @Test
    void updateEpic() throws IOException, InterruptedException {
        String body1 = """
            {
                "title": "epic1",
                "description": "description1"
            }
            """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        EpicCreateOutput output = gson.fromJson(response.body(), EpicCreateOutput.class);

        String body2 = """
            {
                "title": "epic2",
                "description": "description2"
            }
            """;

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + output.epicId))
                .POST(HttpRequest.BodyPublishers.ofString(body2))
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode());

        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + output.epicId))
                .GET()
                .build();

        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response3.statusCode());

        EpicGetOutput output2 = gson.fromJson(response3.body(), EpicGetOutput.class);
        assertEquals("epic2", output2.title);
        assertEquals("description2", output2.description);
        assertEquals(Status.NEW, output2.status);
    }

    @Test
    void deleteEpic() throws IOException, InterruptedException {
        String body1 = """
            {
                "title": "epic1",
                "description": "description1"
            }
            """;

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        EpicCreateOutput output = gson.fromJson(response.body(), EpicCreateOutput.class);

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + output.epicId))
                .DELETE()
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode());

        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + output.epicId))
                .GET()
                .build();

        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response3.statusCode());
    }
}

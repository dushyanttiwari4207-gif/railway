import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RailwayBackend {

    private static final int PORT = 8080;

    private static final Path PROJECT_ROOT = findProjectRoot();
    private static final Path FRONTEND_FOLDER = PROJECT_ROOT.resolve("frontend");
    private static final Path C_FOLDER = PROJECT_ROOT.resolve("c-backend");
    private static final Path EXE_PATH = C_FOLDER.resolve("railway.exe");

    private static Path findProjectRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();

        if (Files.isDirectory(current.resolve("frontend"))
                && Files.isDirectory(current.resolve("c-backend"))) {
            return current;
        }

        Path parent = current.getParent();
        if (parent != null
                && Files.isDirectory(parent.resolve("frontend"))
                && Files.isDirectory(parent.resolve("c-backend"))) {
            return parent;
        }

        return current;
    }

    private static String runCProgram(String input) {
        StringBuilder output = new StringBuilder();

        try {
            if (!Files.exists(EXE_PATH)) {
                return "ERROR: railway.exe was not found at:\n" + EXE_PATH;
            }

            ProcessBuilder processBuilder =
                    new ProcessBuilder(EXE_PATH.toString());

            processBuilder.directory(C_FOLDER.toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            process.getOutputStream(),
                            StandardCharsets.UTF_8))) {

                writer.write(input);
                writer.flush();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return "ERROR: C backend exited with code " + exitCode
                        + "\n" + output;
            }

            return removeMenu(output.toString());

        } catch (Exception e) {
            return "ERROR: Could not start C backend.\n"
                    + e.getMessage();
        }
    }

    private static String removeMenu(String result) {
        int start = result.indexOf("1. Book Ticket");
        int end = result.indexOf("Enter your choice:");

        if (start >= 0 && end >= 0) {
            end += "Enter your choice:".length();
            return result.substring(0, start) + result.substring(end);
        }

        return result;
    }

    private static void sendResponse(
            HttpExchange exchange,
            String response,
            int statusCode) throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set(
                "Content-Type", "text/plain; charset=UTF-8");

        byte[] data = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, data.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }
    }

    private static void sendResponse(
            HttpExchange exchange,
            String response) throws IOException {
        sendResponse(exchange, response, 200);
    }

    private static Map<String, String> parseFormData(String data) {
        Map<String, String> map = new HashMap<>();

        if (data == null || data.isEmpty()) {
            return map;
        }

        for (String pair : data.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {
                String key = URLDecoder.decode(
                        parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(
                        parts[1], StandardCharsets.UTF_8);

                map.put(key, value);
            }
        }

        return map;
    }

    private static void handleBooking(
            HttpExchange exchange) throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);

        Map<String, String> data = parseFormData(body);

        String name = data.getOrDefault("name", "");
        String age = data.getOrDefault("age", "");
        String gender = data.getOrDefault("gender", "");
        String trainNumber = data.getOrDefault("trainNumber", "");
        String trainName = data.getOrDefault("trainName", "");
        String source = data.getOrDefault("source", "");
        String destination = data.getOrDefault("destination", "");
        String date = data.getOrDefault("date", "");
        String className = data.getOrDefault("className", "");
        String preference = data.getOrDefault("preference", "");

        String cInput =
                "1\n"
                + name + "\n"
                + age + "\n"
                + gender + "\n"
                + trainNumber + "\n"
                + trainName + "\n"
                + source + "\n"
                + destination + "\n"
                + date + "\n"
                + className + "\n"
                + preference + "\n";

        String result = runCProgram(cInput);

        if (result.startsWith("ERROR:")) {
            sendResponse(exchange, result, 500);
        } else {
            sendResponse(exchange, result);
        }
    }

    private static void handleSearch(
            HttpExchange exchange) throws IOException {

        String query = exchange.getRequestURI().getQuery();
        String pnr = "";

        if (query != null) {
            for (String parameter : query.split("&")) {
                String[] parts = parameter.split("=", 2);

                if (parts.length == 2
                        && parts[0].equals("pnr")) {
                    pnr = URLDecoder.decode(
                            parts[1], StandardCharsets.UTF_8);
                    break;
                }
            }
        }

        if (pnr.isEmpty()) {
            sendResponse(exchange, "Please provide a PNR.", 400);
            return;
        }

        String result = runCProgram(
                "3\n" + pnr + "\n");

        sendResponse(exchange, result);
    }

    private static void handleReservations(
            HttpExchange exchange) throws IOException {

        sendResponse(exchange, runCProgram("2\n"));
    }

    private static void handleSchedule(
            HttpExchange exchange) throws IOException {

        sendResponse(exchange, runCProgram("4\n"));
    }

    private static void serveFrontend(
            HttpExchange exchange) throws IOException {

        String requestPath = exchange.getRequestURI().getPath();

        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }

        String relativePath = requestPath.substring(1);

        Path filePath = FRONTEND_FOLDER
                .resolve(relativePath)
                .normalize();

        if (!filePath.startsWith(FRONTEND_FOLDER.normalize())
                || !Files.exists(filePath)
                || Files.isDirectory(filePath)) {
            sendResponse(exchange, "404 - File Not Found", 404);
            return;
        }

        String contentType = getContentType(filePath);

        exchange.getResponseHeaders().set(
                "Content-Type", contentType);

        byte[] data = Files.readAllBytes(filePath);

        exchange.sendResponseHeaders(200, data.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }
    }

    private static String getContentType(Path filePath) {
        String name = filePath.getFileName().toString().toLowerCase();

        if (name.endsWith(".html")) return "text/html; charset=UTF-8";
        if (name.endsWith(".css")) return "text/css; charset=UTF-8";
        if (name.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".webp")) return "image/webp";

        return "application/octet-stream";
    }

    public static void main(String[] args) {
        try {
            if (!Files.isDirectory(FRONTEND_FOLDER)) {
                throw new IOException(
                        "Frontend folder not found: " + FRONTEND_FOLDER);
            }

            if (!Files.exists(EXE_PATH)) {
                throw new IOException(
                        "C executable not found: " + EXE_PATH);
            }

            HttpServer server = HttpServer.create(
                    new InetSocketAddress(PORT), 0);

            server.createContext("/api/book", exchange -> {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handleBooking(exchange);
                } else {
                    sendResponse(exchange, "Use POST for booking.", 405);
                }
            });

            server.createContext("/api/search", exchange -> {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handleSearch(exchange);
                } else {
                    sendResponse(exchange, "Use GET for search.", 405);
                }
            });

            server.createContext("/api/reservations", exchange -> {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handleReservations(exchange);
                } else {
                    sendResponse(exchange, "Use GET for reservations.", 405);
                }
            });

            server.createContext("/api/schedule", exchange -> {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handleSchedule(exchange);
                } else {
                    sendResponse(exchange, "Use GET for schedule.", 405);
                }
            });

            server.createContext("/", RailwayBackend::serveFrontend);

            server.start();

            System.out.println("==========================================");
            System.out.println("   INDIAN RAILWAY RESERVATION SYSTEM");
            System.out.println("==========================================");
            System.out.println("Java backend started successfully.");
            System.out.println("Project root: " + PROJECT_ROOT);
            System.out.println("Website: http://localhost:" + PORT);
            System.out.println("Waiting for requests...");

        } catch (Exception e) {
            System.err.println("SERVER STARTUP ERROR:");
            e.printStackTrace();
        }
    }
}

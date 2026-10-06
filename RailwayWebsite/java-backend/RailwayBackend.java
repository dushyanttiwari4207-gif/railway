import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RailwayBackend {

    // =========================================================
    // PATHS
    // =========================================================

    static String EXE_PATH =
            "C:/Users/HP/OneDrive/Desktop/RailwayWebsite/c-backend/railway.exe";

    static String C_FOLDER =
            "C:/Users/HP/OneDrive/Desktop/RailwayWebsite/c-backend";

    static String FRONTEND_FOLDER =
            "C:/Users/HP/OneDrive/Desktop/RailwayWebsite/frontend";


    // =========================================================
    // RUN C PROGRAM
    // =========================================================

    public static String runCProgram(String input) {

        StringBuilder output = new StringBuilder();

        try {

            ProcessBuilder pb = new ProcessBuilder(EXE_PATH);

            // Very important:
            // C program will use Railway.dat from this folder
            pb.directory(new File(C_FOLDER));

            Process process = pb.start();

            BufferedWriter writer =
                    new BufferedWriter(
                            new OutputStreamWriter(process.getOutputStream())
                    );

            writer.write(input);
            writer.flush();
            writer.close();


            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                output.append(line);
                output.append("\n");
            }

            reader.close();

            process.waitFor();


            String result = output.toString();


            // Remove C menu from website output
            int start = result.indexOf("1. Book Ticket");
            int end = result.indexOf("Enter your choice:");

            if (start != -1 && end != -1) {

                end = end + "Enter your choice:".length();

                result =
                        result.substring(0, start)
                        + result.substring(end);
            }


            return result;

        } catch (Exception e) {

            return "ERROR: " + e.getMessage();
        }
    }


    // =========================================================
    // SEND RESPONSE
    // =========================================================

    public static void sendResponse(
            HttpExchange exchange,
            String response) throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        byte[] data =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                200,
                data.length
        );

        OutputStream output =
                exchange.getResponseBody();

        output.write(data);

        output.close();
    }


    // =========================================================
    // PARSE FORM DATA
    // =========================================================

    public static Map<String, String> parseFormData(
            String data) {

        Map<String, String> map =
                new HashMap<>();

        String[] pairs =
                data.split("&");

        for (String pair : pairs) {

            String[] parts =
                    pair.split("=", 2);

            if (parts.length == 2) {

                try {

                    String key =
                            URLDecoder.decode(
                                    parts[0],
                                    StandardCharsets.UTF_8
                            );

                    String value =
                            URLDecoder.decode(
                                    parts[1],
                                    StandardCharsets.UTF_8
                            );

                    map.put(key, value);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }

        return map;
    }


    // =========================================================
    // BOOKING
    // =========================================================

    public static void handleBooking(
            HttpExchange exchange) throws IOException {

        InputStream inputStream =
                exchange.getRequestBody();

        String body =
                new String(
                        inputStream.readAllBytes(),
                        StandardCharsets.UTF_8
                );


        Map<String, String> data =
                parseFormData(body);


        String name =
                data.getOrDefault("name", "");

        String age =
                data.getOrDefault("age", "");

        String gender =
                data.getOrDefault("gender", "");

        String trainNumber =
                data.getOrDefault("trainNumber", "");

        String trainName =
                data.getOrDefault("trainName", "");

        String source =
                data.getOrDefault("source", "");

        String destination =
                data.getOrDefault("destination", "");

        String date =
                data.getOrDefault("date", "");

        String className =
                data.getOrDefault("className", "");

        String preference =
                data.getOrDefault("preference", "");


        // Input sent to C program
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


        String result =
                runCProgram(cInput);


        sendResponse(
                exchange,
                result
        );
    }


    // =========================================================
    // SEARCH PNR
    // =========================================================

    public static void handleSearch(
            HttpExchange exchange) throws IOException {

        String query =
                exchange.getRequestURI().getQuery();

        String pnr = "";


        if (query != null) {

            String[] parts =
                    query.split("=");

            if (parts.length == 2) {

                pnr =
                        URLDecoder.decode(
                                parts[1],
                                StandardCharsets.UTF_8
                        );
            }
        }


        String cInput =
                "3\n"
                + pnr
                + "\n";


        String result =
                runCProgram(cInput);


        sendResponse(
                exchange,
                result
        );
    }


    // =========================================================
    // RESERVED TICKETS
    // =========================================================

    public static void handleReservations(
            HttpExchange exchange) throws IOException {

        String result =
                runCProgram("2\n");


        sendResponse(
                exchange,
                result
        );
    }


    // =========================================================
    // CLEAN C STRING
    //
    // This removes garbage binary characters from the
    // fixed-size character arrays stored by the C program.
    // =========================================================

    public static String cleanCString(
            byte[] data,
            int start,
            int length) {

        StringBuilder text =
                new StringBuilder();


        for (
                int i = start;
                i < start + length && i < data.length;
                i++
        ) {

            int value =
                    data[i] & 0xFF;


            // NULL character means
            // the actual C string has ended
            if (value == 0) {

                break;
            }


            // Keep only normal printable characters
            if (value >= 32 && value <= 126) {

                text.append(
                        (char) value
                );

            } else {

                // Binary garbage found
                break;
            }
        }


        return text.toString().trim();
    }


    // =========================================================
    // TRAIN SCHEDULE
    //
    // Reads Railway.dat directly.
    //
    // C structure:
    //
    // name          -> 100 bytes
    // age           -> 4 bytes
    // gender        -> 100 bytes
    // tnumber       -> 4 bytes
    // tname         -> 100 bytes
    //
    // tnumber offset = 204
    // tname offset   = 208
    //
    // Total structure size = 816 bytes
    // =========================================================

    public static void handleSchedule(
            HttpExchange exchange) throws IOException {


        String filePath =
                C_FOLDER + "/Railway.dat";


        StringBuilder output =
                new StringBuilder();


        // Schedule heading
        output.append(
                String.format(
                        "%-12s %-30s %-20s %-20s%n",
                        "Train No",
                        "Train Name",
                        "Departure",
                        "Arrival"
                )
        );


        output.append(
                "--------------------------------------------------------------------------\n"
        );


        int recordSize = 816;


        // Used to prevent the same train appearing repeatedly
        HashSet<Integer> displayedTrains =
                new HashSet<>();


        try (
                RandomAccessFile file =
                        new RandomAccessFile(
                                filePath,
                                "r"
                        )
        ) {


            long fileSize =
                    file.length();


            while (
                    file.getFilePointer()
                    + recordSize
                    <= fileSize
            ) {


                // Read one complete C structure
                byte[] record =
                        new byte[recordSize];


                file.readFully(record);


                // C compiler on Windows stores integers
                // in little-endian format
                ByteBuffer buffer =
                        ByteBuffer.wrap(record);

                buffer.order(
                        ByteOrder.LITTLE_ENDIAN
                );


                // tnumber is at byte 204
                int trainNumber =
                        buffer.getInt(204);


                // tname starts at byte 208
                String trainName =
                        cleanCString(
                                record,
                                208,
                                100
                        );


                // Ignore invalid records
                if (
                        trainNumber <= 0
                        || trainName.isEmpty()
                ) {

                    continue;
                }


                // If this train has already been displayed,
                // don't display it again
                if (
                        displayedTrains.contains(
                                trainNumber
                        )
                ) {

                    continue;
                }


                displayedTrains.add(
                        trainNumber
                );


                // -------------------------------------------------
                // Generate a display time from train number
                // -------------------------------------------------

                int departureHour =
                        Math.abs(trainNumber) % 24;


                int departureMinute =
                        Math.abs(trainNumber) % 60;


                int arrivalHour =
                        (departureHour + 4) % 24;


                int arrivalMinute =
                        (departureMinute + 4) % 60;


                String departure =
                        String.format(
                                "%02d:%02d",
                                departureHour,
                                departureMinute
                        );


                String arrival =
                        String.format(
                                "%02d:%02d",
                                arrivalHour,
                                arrivalMinute
                        );


                // -------------------------------------------------
                // Add row to schedule
                // -------------------------------------------------

                output.append(
                        String.format(
                                "%-12d %-30s %-20s %-20s%n",
                                trainNumber,
                                trainName,
                                departure,
                                arrival
                        )
                );
            }


        } catch (FileNotFoundException e) {


            output.append(
                    "\nNo Railway.dat file found.\n"
            );


        } catch (Exception e) {


            output.append(
                    "\nError reading schedule: "
            );

            output.append(
                    e.getMessage()
            );

            output.append(
                    "\n"
            );
        }


        sendResponse(
                exchange,
                output.toString()
        );
    }


    // =========================================================
    // SERVE FRONTEND
    // =========================================================

    public static void serveFrontend(
            HttpExchange exchange) throws IOException {


        String requestPath =
                exchange.getRequestURI().getPath();


        // Homepage
        if (requestPath.equals("/")) {

            requestPath =
                    "/index.html";
        }


        String fileName =
                requestPath.substring(1);


        Path filePath =
                Paths.get(
                        FRONTEND_FOLDER,
                        fileName
                );


        // File not found
        if (
                !Files.exists(filePath)
                || Files.isDirectory(filePath)
        ) {


            String message =
                    "404 - File Not Found";


            byte[] data =
                    message.getBytes(
                            StandardCharsets.UTF_8
                    );


            exchange.sendResponseHeaders(
                    404,
                    data.length
            );


            OutputStream output =
                    exchange.getResponseBody();


            output.write(data);

            output.close();

            return;
        }


        // Default content type
        String contentType =
                "text/plain; charset=UTF-8";


        // HTML
        if (fileName.endsWith(".html")) {

            contentType =
                    "text/html; charset=UTF-8";
        }


        // CSS
        else if (fileName.endsWith(".css")) {

            contentType =
                    "text/css; charset=UTF-8";
        }


        // JavaScript
        else if (fileName.endsWith(".js")) {

            contentType =
                    "application/javascript; charset=UTF-8";
        }


        // PNG
        else if (fileName.endsWith(".png")) {

            contentType =
                    "image/png";
        }


        // JPG
        else if (
                fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
        ) {

            contentType =
                    "image/jpeg";
        }


        // WEBP
        else if (fileName.endsWith(".webp")) {

            contentType =
                    "image/webp";
        }


        // GIF
        else if (fileName.endsWith(".gif")) {

            contentType =
                    "image/gif";
        }


        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType
        );


        byte[] data =
                Files.readAllBytes(filePath);


        exchange.sendResponseHeaders(
                200,
                data.length
        );


        OutputStream output =
                exchange.getResponseBody();


        output.write(data);

        output.close();
    }


    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {


        try {


            HttpServer server =
                    HttpServer.create(
                            new InetSocketAddress(8080),
                            0
                    );


            // -------------------------------------------------
            // BOOKING API
            // -------------------------------------------------

            server.createContext(
                    "/api/book",
                    exchange -> {

                        if (
                                exchange
                                .getRequestMethod()
                                .equalsIgnoreCase("POST")
                        ) {

                            handleBooking(
                                    exchange
                            );

                        } else {

                            sendResponse(
                                    exchange,
                                    "Use POST for booking."
                            );
                        }
                    }
            );


            // -------------------------------------------------
            // SEARCH API
            // -------------------------------------------------

            server.createContext(
                    "/api/search",
                    exchange -> {

                        if (
                                exchange
                                .getRequestMethod()
                                .equalsIgnoreCase("GET")
                        ) {

                            handleSearch(
                                    exchange
                            );

                        } else {

                            sendResponse(
                                    exchange,
                                    "Use GET for search."
                            );
                        }
                    }
            );


            // -------------------------------------------------
            // RESERVATIONS API
            // -------------------------------------------------

            server.createContext(
                    "/api/reservations",
                    exchange -> {

                        handleReservations(
                                exchange
                        );
                    }
            );


            // -------------------------------------------------
            // SCHEDULE API
            // -------------------------------------------------

            server.createContext(
                    "/api/schedule",
                    exchange -> {

                        handleSchedule(
                                exchange
                        );
                    }
            );


            // -------------------------------------------------
            // FRONTEND
            // -------------------------------------------------

            server.createContext(
                    "/",
                    exchange -> {

                        serveFrontend(
                                exchange
                        );
                    }
            );


            // Start server
            server.start();


            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "     INDIAN RAILWAY RESERVATION SYSTEM"
            );

            System.out.println(
                    "=========================================="
            );

            System.out.println();


            System.out.println(
                    "Java Backend Started Successfully!"
            );

            System.out.println();


            System.out.println(
                    "Website:"
            );

            System.out.println(
                    "http://localhost:8080"
            );

            System.out.println();


            System.out.println(
                    "Waiting for requests..."
            );


        } catch (Exception e) {


            e.printStackTrace();
        }
    }
}
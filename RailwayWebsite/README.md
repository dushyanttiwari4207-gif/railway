# Railway Reservation System

A railway reservation web application using:

- **Frontend:** HTML, CSS, JavaScript
- **Web/API backend:** Java `HttpServer`
- **Core reservation logic:** C
- **Storage:** `Railway.dat` binary file

## Project structure

```
RailwayWebsite/
├── frontend/
│   ├── index.html
│   ├── style.css
│   ├── script.js
│   ├── railway-logo.png
│   └── vande-bharat.jpg
│
├── c-backend/
│   ├── railway.c
│   ├── railway.exe
│   └── Railway.dat
│
└── java-backend/
    └── RailwayBackend.java
```

## System flow

```
Browser
   |
   | HTTP
   v
Java HttpServer :8080
   |
   | ProcessBuilder
   v
railway.exe
   |
   | fread / fwrite
   v
Railway.dat
```

## Run on Windows

Open the `RailwayWebsite` folder in VS Code.

### 1. Check Java

```powershell
java -version
javac -version
```

Java 11+ is recommended.

### 2. Compile the C backend

If GCC/MinGW is installed:

```powershell
cd c-backend
gcc railway.c -o railway.exe
cd ..
```

The repository also contains a Windows `railway.exe`.

### 3. Compile Java

```powershell
cd java-backend
javac RailwayBackend.java
```

### 4. Start the server

From `java-backend`:

```powershell
java RailwayBackend
```

You should see:

```
Java backend started successfully.
Website: http://localhost:8080
Waiting for requests...
```

### 5. Open the website

Open `http://localhost:8080`.

Do not open `index.html` directly. The Java server must be running because the frontend communicates with the Java API.

## API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/book` | Book a ticket |
| GET | `/api/search?pnr=PNR` | Search by PNR |
| GET | `/api/reservations` | View reservations |
| GET | `/api/schedule` | View train schedule |

## Important

The application currently uses a Windows `.exe`, so it is designed to run on Windows. The Java server must be running before opening the website.

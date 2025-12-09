# Docker Desktop Setup & Running Redis

## Download Docker Desktop

**Download Link:** https://www.docker.com/products/docker-desktop

Select your OS and download the installer.

## Step 1: After Downloading Docker Desktop

1. **Install Docker Desktop**

   - Run the downloaded installer
   - Follow the installation steps
   - Restart your computer if asked
2. **Open Docker Desktop**

   - Find Docker icon in Applications/Start Menu
   - Click to launch Docker Desktop
   - Wait for it to start (you'll see the whale icon in taskbar)
   - Wait 2-3 minutes for Docker daemon to start
3. **Verify Docker is Running**

   ```bash
   docker --version
   ```

   Should show something like: `Docker version 27.0.0`

## Step 2: Run Redis with Docker

Open your terminal/command prompt and run:

```bash
cd HomeMate/homemate-backend
docker-compose up -d
```

This command:

- Reads `compose.yaml` file
- Downloads Redis image (first time only)
- Starts Redis container in background
- Exposes port 6379

**Verify Redis is running:**

```bash
docker ps
```

Should show a container with `redis` in the name.

## Step 3: Run the Application

In the same terminal:

```bash
./mvnw spring-boot:run
```

Wait for:

```
Started HomeMateApplication in X seconds
Tomcat started on port 8080
```

## Common Issues

### Port already in use

```bash
docker-compose down
docker-compose up -d
```

## What Each Command Does


| Command                     | What It Does              |
| --------------------------- | ------------------------- |
| `docker-compose up -d`      | Start Redis in background |
| `docker ps`                 | Show running containers   |
| `docker-compose logs redis` | View Redis logs           |
| `docker-compose down`       | Stop Redis                |
| `./mvnw spring-boot:run`    | Run Java application      |

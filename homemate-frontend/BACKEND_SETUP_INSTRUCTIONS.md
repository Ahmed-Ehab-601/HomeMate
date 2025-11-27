# Backend Controller Setup Instructions

## Option 1: Simple Controller (Recommended for Quick Start)

Use `BACKEND_CONTROLLER_SIMPLE.java` - This matches your existing code structure.

**Key Points:**
- No `@RequestMapping` base path (endpoints are at root level)
- `@CrossOrigin(origins = "http://localhost:5173")` - Allows frontend to access
- Endpoints:
  - `GET /getallservices`
  - `GET /getservicedetails/{serviceID}`
  - `POST /create`
  - `POST /edit/{id}`
  - `DELETE /delete/{id}`

**Frontend API URL:** `http://localhost:8080` (already configured)

---

## Option 2: Controller with Base Path

If you use `BACKEND_CONTROLLER_EXAMPLE.java` with `@RequestMapping("/api/services")`:

**Endpoints become:**
- `GET /api/services/getallservices`
- `GET /api/services/getservicedetails/{serviceID}`
- `POST /api/services/create`
- `POST /api/services/edit/{id}`
- `DELETE /api/services/delete/{id}`

**Update Frontend:** Change `API_BASE_URL` in `src/services/api.js` to:
```javascript
const API_BASE_URL = 'http://localhost:8080/api/services';
```

---

## CORS Configuration

### Method 1: Using @CrossOrigin on Controller (Simplest)
```java
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceManagementController {
    // ...
}
```

### Method 2: Global CORS Configuration (Better for multiple controllers)
Create `BACKEND_CORS_CONFIG.java` and add it to your project.

---

## ServiceDto Structure Expected by Frontend

Your `ServiceDto` should have these fields:
```java
public class ServiceDto {
    private Long id;              // or serviceID
    private String name;
    private String description;
    private String imageData;     // Base64 encoded image
    private String imageName;
    private String imageType;
    private Integer completedTasks; // Optional
    // getters and setters
}
```

---

## ServiceDetailsDto Structure

```java
public class ServiceDetailsDto {
    private ServiceDto service;
    private Integer completedTasks;
    private Integer taskers;
    // getters and setters
}
```

---

## Testing

1. Start your Spring Boot backend on port 8080
2. Start your React frontend (should be on port 5173)
3. Check browser console for any CORS errors
4. If you see CORS errors, make sure `@CrossOrigin` is added to your controller

---

## Common Issues

**Issue:** CORS error in browser
**Solution:** Add `@CrossOrigin(origins = "http://localhost:5173")` to your controller

**Issue:** 404 Not Found
**Solution:** Check that your endpoints match exactly (case-sensitive)

**Issue:** 500 Internal Server Error
**Solution:** Check your service layer implementation and database connection


# Smart Campus Sensor & Room Management API

#### **Name:**   Dinura Sasmitha 
#### **UoW ID:** w2120344
#### **IIT ID:** 20230043
---

## Project Overview
The Smart Campus API is a high-performance, RESTful web service built using **Java JAX-RS (Jakarta RESTful Web Services)**. It serves as the backend infrastructure for managing campus facilities, specifically focusing on `Rooms` and various `Sensors` (e.g., Temperature, CO2) deployed within them. The API emphasizes standard REST architectural patterns, strict HTTP status code compliance, in-memory data management, and defensive error handling.

---

## How to Build and Run the Project

### Prerequisites
* Java Development Kit
* Apache Maven
* A lightweight Servlet Container or Java EE Application Server (Apache Tomcat)
* IDE (e.g., Apache NetBeans, IntelliJ IDEA)

---

### Step-by-Step Instructions
1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/Dinura-labs/smart-campus-api.git](https://github.com/Dinura-labs/smart-campus-api.git)
   cd smart-campus-api

2. **Build the Project using Maven:**
   ```bash
   mvn clean install

3. **Deploy to Server:**
* Open the project in your IDE (NetBeans)
* Ensure a local server (Tomcat) is configured in your IDE
* Right-click the project and select Run or Deploy

4. **Base Application Path:**
   * Once the server is running, the API will be accessible at:
   ```bash
   http://localhost:8080/smart-campus-api/api/v1

---

## Sample cURL Commands
Here are five working examples to test the core functionalities of the API. You can run these in your terminal or command prompt:
1. **View API Discovery Metadata (HATEOAS)**
   ```bash
   curl -X GET http://localhost:8080/smart-campus-api/api/v1/

2. **Create a New Room**
   ```bash
   curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
   -H "Content-Type: application/json" \
   -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'

3. **List All Rooms**
   ```bash
   curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms

4. **Register a New Sensor to a Room**
   ```bash
   curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
   -H "Content-Type: application/json" \
   -d '{"id": "CO2-001", "type": "CO2", "status": "ACTIVE", "roomId": "LIB-301"}'

5. **Retrieve Sensors Filtered by Type**
   ```bash
   curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"

---

# Conceptual Report

## Part 1: Service Architecture & Setup
### 1.1 Lifecycle and Data Synchronization
* In my implementation, I recognized that JAX-RS resources operate on a per-request lifecycle by default. This means a new instance is created for every HTTP request. Since I am not using a persistent database, storing data in instance variables would lead to immediate data loss
To handle this, I utilized static data structures housed in a central dataStore. To prevent race conditions in a multi threaded environment, I chose ConcurrentHashMap. This ensures that even if multiple clients attempt to register sensors simultaneously, the internal state remains consistent and thread safe

### 1.2 HATEOAS (Hypermedia) Benefits
* I implemented a Discovery endpoint to provide Hypermedia links. This is a hallmark of REST maturity because it makes the API self-documenting. Instead of client developers relying on static, potentially outdated documentation, they can programmatically discover available endpoints. This decoupling allows the server to evolve its URI structure without breaking client applications that follow the hypermedia links

---

## Part 2: Room Management
### 2.1 Bandwidth vs. Processing (IDs vs. Full Objects)
* When listing rooms, returning only IDs is highly bandwidth efficient but leads to the "N+1 Request Problem," where the client must make dozens of follow up calls to get actual details. I chose to return full room objects. While this slightly increases the payload size, it drastically improves the user experience by reducing network round trips and allowing the client to render the UI immediately

### 2.2 Idempotency in Deletion
* My DELETE operation is fully idempotent. When a client sends a DELETE request, the room is removed and a 204 No Content is returned. If the same request is sent again, the system identifies the room is already gone and returns a 404 Not Found. Crucially, the state of the server remains unchanged after the first successful deletion, regardless of how many times the client repeats the request

---

## Part 3: Sensor Operations & Linking
### 3.1 Handling Media Type Mismatch
* By applying the @Consumes(MediaType.APPLICATION_JSON) annotation, I've implemented a strict filter at the framework level. If a client sends an unsupported format like XML, JAX-RS automatically intercepts this and returns an HTTP 415 Unsupported Media Type. This acts as a first line of defense, ensuring my business logic only processes valid, expected data formats

### 3.2 Query Parameters vs. Path Parameters
* I used @QueryParam for sensor filtering because "type" is a filter criteria, not a unique resource identifier. In REST best practices, path parameters are for identifying specific objects (e.g., /sensors/001), whereas query parameters are for modifying the view of a collection (e.g., sorting or searching). This makes the API more flexible for future expansion

---

## Part 4: Deep Nesting with Sub-Resources
### 4.1 The Sub-Resource Locator Pattern
* Managing everything in a single class leads to "God Objects" that are impossible to maintain. I used the Sub-Resource Locator pattern to delegate reading specific logic to a dedicated SensorReadingResource. This modular approach improves code readability and adheres to the Single Responsibility Principle, making it much easier to test and scale

---

## Part 5: Advanced Error Handling, Exception Mapping & Logging
### 5.1 Semantic Accuracy of HTTP 422
* When a client sends a valid JSON body that references a non-existent roomId, a standard 404 can be confusing because the endpoint itself was found. I chose HTTP 422 Unprocessable Entity because it semantically communicates that while the request syntax is perfect, the internal business logic (the foreign key reference) is invalid.

### 5.2 Cybersecurity Risks of Stack Traces
* Exposing raw Java stack traces is a significant security risk known as Information Exposure. It reveals internal package names, framework versions (Tomcat), and file paths. An attacker could use this metadata to identify specific vulnerabilities (CVEs) or map the server's internal architecture. I implemented a Global Exception Mapper to intercept all throwables and return a generic, safe JSON error instead.

### 5.3 Advantages of JAX-RS Filters
* I implemented logging via JAX-RS Filters rather than manual statements. This handles logging as a cross-cutting concern. It ensures 100% observability across all endpoints without cluttering the business logic. It follows the DRY (Don't Repeat Yourself) principle, making the logging policy consistent and easy to update from a single class.


# Smart Campus Sensor & Room Management API

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
   curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms
   -H "Content-Type: application/json"
   -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'

3. **List All Rooms**
   ```bash
   curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms

4. **Register a New Sensor to a Room**
   ```bash
   curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors
   -H "Content-Type: application/json"
   -d '{"id": "CO2-001", "type": "CO2", "status": "ACTIVE", "roomId": "LIB-301"}'

5. **Retrieve Sensors Filtered by Type**
   ```bash
   curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"



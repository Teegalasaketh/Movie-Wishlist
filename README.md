# 🎬 Movie Wishlist Application

A Spring Boot web application for managing a personalized movie wishlist.  
Built using a clean layered architecture with controllers, services, repositories, and security.

---

## 📂 Project Structure

src
└── main
├── java/com/moviewishlist
│ ├── config
│ ├── controller
│ ├── model
│ ├── repository
│ ├── security
│ ├── service
│ └── MovieWishlistApp.java
└── resources
├── static
├── templates
└── application.properties
└── test


---

## 🚀 Features

- Add movies to a wishlist  
- View and manage saved movies  
- Layered architecture (Controller → Service → Repository)  
- Spring Security integration  
- Thymeleaf-based UI  
- Docker support  

---

## 🛠️ Tech Stack

- Java  
- Spring Boot  
- Spring MVC  
- Spring Data JPA  
- Spring Security  
- Thymeleaf  
- Maven  
- Docker  

---

## ⚙️ Configuration

Edit the configuration file:

## src/main/resources/application.properties


Example:
properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/moviewishlist
spring.datasource.username=root
spring.datasource.password=password

##  ▶️ Run the Application
Using Maven
mvn clean install
mvn spring-boot:run

Using Docker
docker build -t moviewishlist .
docker run -p 8080:8080 moviewishlist

## 🌐 Access the Application

Open your browser and go to: http://localhost:8080

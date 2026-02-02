# 🎬 Movie Wishlist Application

A Spring Boot web application for managing a personalized movie wishlist.
Built using a clean layered architecture with controllers, services, repositories, and security.

---

## 📂 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── moviewishlist/
│   │           ├── config/
│   │           ├── controller/
│   │           ├── model/
│   │           ├── repository/
│   │           ├── security/
│   │           ├── service/
│   │           └── MovieWishlistApp.java
│   └── resources/
│       ├── static/
│       ├── templates/
│       └── application.properties
└── test/
```

---

## 🚀 Features

* Add movies to a wishlist
* View and manage saved movies
* Layered architecture (Controller → Service → Repository)
* Spring Security integration
* Thymeleaf-based UI
* Docker support

---

## 🛠️ Tech Stack

* Java
* Spring Boot
* Spring MVC
* Spring Data JPA
* Spring Security
* Thymeleaf
* Maven
* Docker

---

## 🧩 Sample Domain Model

```java
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String genre;
    private int year;
}
```

---

## 🌐 API Endpoints

| Method | Endpoint     | Description    |
| ------ | ------------ | -------------- |
| GET    | /movies      | View wishlist  |
| POST   | /movies      | Add a movie    |
| DELETE | /movies/{id} | Remove a movie |

---

## 🔐 Security

* Spring Security enabled
* Form-based authentication
* Only authenticated users can access wishlist pages

Default login page:

```
/login
```

---

## ⚙️ Configuration

Edit the configuration file:

```
src/main/resources/application.properties
```

Example:

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/moviewishlist
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

---

## ▶️ Run the Application

### Using Maven

```bash
mvn clean install
mvn spring-boot:run
```

### Using Docker

```bash
docker build -t moviewishlist .
docker run -p 8080:8080 moviewishlist
```

> ⚠️ Ensure MySQL is running before starting the application, or use Docker Compose.

---

## 🌐 Access the Application

Open your browser and go to:

```
http://localhost:8080
```

---

## 🔮 Future Enhancements

* User-specific wishlists
* External movie API integration
* Ratings and personal notes
* Pagination and sorting
* Docker Compose (App + MySQL)

---

## 📄 License

This project is for educational purposes.

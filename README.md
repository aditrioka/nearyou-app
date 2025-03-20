# NearYou ID

A location-based social media application built with Spring Boot and Kotlin.

## Overview

NearYou ID is a social platform that connects users based on proximity. The application allows users to create profiles, share location-based posts, discover content from nearby users, communicate through direct messaging, and receive notifications about relevant activities.

## Features

- **User Authentication**: Secure registration and login system
- **User Profiles**: Customizable user profiles with bio and profile picture
- **Location-Based Posts**: Share content tied to specific locations
- **Proximity Feed**: Discover content from users nearby
- **Direct Messaging**: Private communication between users
- **Notifications**: Real-time updates about relevant activities

## Technology Stack

- **Backend**: Spring Boot 3.4.3 with Kotlin 1.9.25
- **Build Tool**: Gradle with Kotlin DSL
- **Database**: PostgreSQL
- **Security**: Spring Security
- **API**: RESTful API architecture

## Getting Started

### Prerequisites

- JDK 17 or higher
- PostgreSQL 14 or higher
- Gradle 8.x

### Database Setup

1. Create a PostgreSQL database:
   ```sql
   CREATE DATABASE nearyou;
   ```

2. Configure database connection in `application.yml`

### Building and Running

```bash
# Clone the repository
git clone https://github.com/your-username/nearyou-app.git
cd nearyou-app

# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will be available at http://localhost:8080/api

## Project Structure

The application follows a domain-driven design approach:

```
nearyou-app/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── id/
│   │   │       └── nearyou/
│   │   │           └── app/
│   │   │               ├── domain/      # Domain entities
│   │   │               ├── repository/  # Data access layer
│   │   │               ├── service/     # Business logic
│   │   │               ├── web/         # Controllers and DTOs
│   │   │               └── config/      # Application configuration
│   │   └── resources/
│   │       └── application.yml
│   └── test/
└── build.gradle.kts
```

## License

[Insert your license here]

## Contact

[Your contact information]

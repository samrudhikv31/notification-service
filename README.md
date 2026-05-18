# Notification Service

A real-time notification microservice built with
Spring Boot, Kafka, WebSocket, MongoDB, and PostgreSQL.
Client applications publish events to Kafka, which
are consumed and delivered to users instantly via
WebSocket, with full notification history stored
in MongoDB.

---

## Tech Stack

- **Java 21**
- **Spring Boot 3.5**
- **PostgreSQL** — users and subscriptions
- **MongoDB** — notification history
- **Redis** — online user tracking
- **Kafka** — event driven messaging
- **WebSocket** — real time delivery
- **JWT** — API security
- **Docker** — containerized infrastructure

---

## Architecture
Client App 
\
│
\
│ POST /api/notifications/publish
\
▼
\
Notification Service
\
│
\
│ publishes to
\
▼\
Kafka Topic (notification-events)\
│\
│ consumed by\
▼\
Notification Consumer\
│\
├──── saves to MongoDB\
│     (notification history)\
│
└──── checks Redis\
(is user online?)\
│\
Yes → sends via WebSocket instantly 🔔\
No  → saved, user sees on next login

---

## Why This Tech Stack?

**Why MongoDB for notifications?**
Every notification type has different fields —
order notifications have orderId and restaurant,
payment notifications have txnId and bank details.
MongoDB's flexible schema handles this perfectly
unlike SQL which would need nullable columns or
separate tables.

**Why Kafka instead of direct API call?**
If we called WebSocket directly without Kafka,
a spike in events could overwhelm the service.
Kafka acts as a buffer — events queue up and
get processed at a controlled rate. This is how
every large scale notification system works.

**Why WebSocket over polling?**
Polling means client asks server "any new
notifications?" every few seconds — wasteful.
WebSocket keeps connection open, server pushes
instantly. Zero unnecessary requests.

**Why Redis for online user tracking?**
Checking if a user is online needs to be
millisecond fast — checked on every notification.
Redis in-memory lookup is perfect for this.
MySQL would be too slow for this use case.

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/users/register | None | Register user |
| POST | /api/notifications/publish | JWT | Publish event |
| GET | /api/notifications/{userId} | JWT | Get all notifications |
| GET | /api/notifications/{userId}/unread | JWT | Get unread |
| GET | /api/notifications/{userId}/unread/count | JWT | Unread count |
| PUT | /api/notifications/{id}/read | JWT | Mark as read |
| GET | /api/health | None | Health check |
| WS | /ws/notifications?userId={id} | None | WebSocket |

---

## Real Time Flow

1. Connect WebSocket:
   ws://localhost:8081/ws/notifications?userId=user123
2. Publish event via API:
```json
POST /api/notifications/publish
{
  "apiKey": "your-api-key",
  "userId": "user123",
  "type": "ORDER_PLACED",
  "title": "Order Confirmed!",
  "message": "Your Dominos order is confirmed",
  "data": {
    "orderId": "ORD123",
    "amount": 450
  }
}
```

3. Notification appears instantly in WebSocket! 🔔

---

## Running Locally

### Start all Docker containers
```bash
docker start redis-ratelimiter \
  postgres-notification \
  mongo-notification \
  zookeeper \
  kafka-notification
```

### Setup database
```bash
# PostgreSQL auto creates tables via JPA
# MongoDB auto creates collections
# Just make sure containers are running!
```

### Configure properties
```bash
cp src/main/resources/application-template.properties \
   src/main/resources/application.properties
# Update with your credentials
```

### Run
```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Duser.timezone=Asia/Kolkata"
```

---

## Project Structure
src/main/java/com/notification/\
├── config/\
│   ├── KafkaConfig.java\
│   ├── MongoConfig.java\
│   ├── PostgresConfig.java\
│   ├── RedisConfig.java\
│   ├── SecurityConfig.java\
│   └── WebSocketConfig.java\
├── controller/\
│   ├── NotificationController.java\
│   ├── UserController.java\
│   └── TestController.java\
├── dto/\
│   ├── NotificationEvent.java\
│   ├── NotificationResponse.java\
│   ├── PublishEventRequest.java\
│   ├── RegisterUserRequest.java\
│   └── RegisterUserResponse.java\
├── exception/\
│   ├── ErrorResponse.java\
│   └── GlobalExceptionHandler.java\
├── kafka/\
│   ├── NotificationConsumer.java\
│   └── NotificationProducer.java\
├── model/\
│   ├── mongodb/\
│   │   └── Notification.java\
│   └── postgresql/\
│       ├── User.java\
│       └── Subscription.java\
├── repository/\
│   ├── mongodb/\
│   │   └── NotificationRepository.java\
│   └── postgresql/\
│       ├── UserRepository.java\
│       └── SubscriptionRepository.java\
├── security/\
│   ├── JwtFilter.java\
│   └── JwtUtil.java\
├── service/\
│   ├── NotificationService.java\
│   └── UserService.java\
└── websocket/\
└── NotificationWebSocketHandler.java\

---

## Author

**Your Name**
- LinkedIn: linkedin.com/in/samrudhi-k-v-17ba78266
- GitHub: github.com/samrudhikv31

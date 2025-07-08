# 🕹️ Tic Tac Toe WebSocket

This is a real-time, two-player Tic Tac Toe game built using WebSockets and Java (Jakarta EE), with a React frontend.  
It supports player turns, persistent game states across rounds, and history playback by step.

## 🧩 Getting Started

These instructions will help you run the project locally for development and testing.

### Prerequisites

You will need the following installed:

- [Java 17+](https://jdk.java.net/)
- [Maven 3.8+](https://maven.apache.org/install.html)
- Git (optional, for cloning)

---

## ⚙️ Backend Setup

### Clone the Repository

```bash
git clone https://github.com/thesrcielos/TicTacToeBack.git
cd TicTacToeBack
```

### Build the Project
```
mvn clean install
```
### Run the Application
```
java -cp target/tictac-0.0.1-SNAPSHOT.jar com.tictac.Application

```
The WebSocket server will run and accept connections at: ws://localhost:8080/bbService


### 🔌 WebSocket Endpoint
* /bbService — Main WebSocket route.

Only allows two players to connect.

Enforces turn-taking.

Maintains state history for previous rounds (stepBack supported).

## 🎨 Coding Style
The Java code follows the Google Java Style Guide.
Frontend code follows React best practices with functional components and hooks.

## 🚀 Deployment
To package the backend:

```
mvn package
```
```
java -cp target/tictactoe-0.0.1-SNAPSHOT.jar com.tictac.Application
```
📚 Javadoc
You can generate documentation with:
```
mvn javadoc:javadoc
```
View the docs in:

target/site/apidocs/index.html
## 🛠️ Built With
* Java 17

* Jakarta WebSocket API

* Maven

* JUnit 4

## 👥 Authors
Diego Armando Macia Diaz – Initial work
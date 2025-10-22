# aonbas2srv

A lightweight Java server built with Netty and Guice, designed for transport-related APIs and Object Transfer Protocol (OTP) delivery.  
It aims to be fast, modular, and easy to integrate with Kotlin Multiplatform clients.

---

## Overview

**aonbas2srv** provides:
- A simple HTTP server with REST endpoints.
- Multiple transport data services (TMB, Metro, etc.).
- An OTP system for secure object transfers between apps.

**Tech stack:**
- Java 21
- Gradle (Kotlin DSL)
- Netty + Guice for dependency injection
- Gson for JSON handling

---

## Setup & Environment

Before running, create a `.env` file in the project root:

```env
TMB_APP_ID=<your-app-id>
TMB_APP_KEY=<your-app-key>
`````
Go to [developer.tmb.cat](https://developer.tmb.cat) and create and application to use the TMB API<br>
More apis are to come with the development

Environment variables are loaded automatically using dotenv-java.
You can access them in code via:

```java
Dotenv dotenv = Dotenv.load();
String apiKey = dotenv.get("TMB_API_KEY");
```
---
## Build & Run
Build the JAR:

```bash
./gradlew clean build
```
Run the server:

```bash
java -jar build/libs/aonbas2srv-<version>.jar
```

If you’re developing locally:
```bash
./gradlew run
```
---
## Contributing
Contributions are always welcome!
Here’s how you can help:

Fork the repository and create a new branch:

```bash
git checkout -b feat/my-feature
```
Make your changes, keeping commits small and descriptive.

Run tests before submitting:

```bash
./gradlew test
```

Open a Pull Request with a short summary of your update.

**If you find a bug, open an issue describing:**
- What happened
-Steps to reproduce
-Expected vs actual behavior

Maintained by [Lesslie (x341dev)](https://github.com/x341dev)
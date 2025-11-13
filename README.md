# 🚀 aonbas2srv

[![Java Version](https://img.shields.io/badge/Java-21-brightgreen)](https://www.oracle.com/java/)
[![Build Status](https://img.shields.io/github/actions/workflow/status/x341dev/aonbas2srv/build.yml?branch=main)](https://github.com/x341dev/aonbas2srv/actions)
[![License](https://img.shields.io/github/license/x341dev/aonbas2srv)](LICENSE)
[![Issues](https://img.shields.io/github/issues/x341dev/aonbas2srv)](https://github.com/x341dev/aonbas2srv/issues)

**A lightweight Java server built with Netty and Guice, designed for transport-related APIs and OTP delivery.**  
Fast, modular, and ready for integration with Kotlin Multiplatform clients.

---

## 🧩 Overview

**aonbas2srv** features:

- ✅ Simple HTTP server with REST endpoints
- ✅ Multiple transport data services (TMB, Metro, etc.)
- ✅ OTP system for secure object transfers

**Tech stack:**

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Build | Gradle (Kotlin DSL) |
| Server | Netty |
| DI | Guice |
| JSON | Gson |

---

## ⚙️ Setup & Environment

1. Create a `.env` file in the project root:

```env
TMB_APP_ID=<your-app-id>
TMB_APP_KEY=<your-app-key>

TRAM_CLIENT_ID=<your-client-id>
TRAM_CLIENT_SECRET=<your-client-secret>
```
Get API credentials:

TMB: [developer.tmb.cat](https://developer.tmb.cat)

TRAM: [opendata.tram.cat](https://opendata.tram.cat)

Access variables in code:

```java
Dotenv dotenv = Dotenv.load();
String apiKey = dotenv.get("TMB_APP_KEY");
```
More APIs will be added over time.

<details> <summary>💡 Example .env for testing</summary>
```env
TMB_APP_ID=test-app
TMB_APP_KEY=abcd1234
TRAM_CLIENT_ID=test-client
TRAM_CLIENT_SECRET=secret1234
```
</details>

## 🏗 Build & Run
Build the JAR:

```bash
./gradlew clean build
```
Run the server:
```bash
java -jar build/libs/aonbas2srv-<version>.jar
```
For local development:
```bash
./gradlew run
```
## 🧪 Testing
Run all tests before committing changes:
```bash
./gradlew test
```
## 🤝 Contributing
We welcome contributions!

Fork the repo and create a new branch:
```bash
git checkout -b feat/my-feature
```
Make small, descriptive commits (use conventional commits).

Run tests and make sure everything passes.

Open a Pull Request with a short summary of your update.

Bug reports
If you find a bug, open an issue with:

What happened

Steps to reproduce

Expected vs actual behavior

## 📦 Releases
We generate releases automatically using [GitHub Actions](https://github.com/x341dev/aonbas2srv/actions) and conventional commits.
Check the Releases page for the latest JARs.

## 📝 Maintainers
Maintained by [Lesslie (x341dev)](https://github.com/x341dev)

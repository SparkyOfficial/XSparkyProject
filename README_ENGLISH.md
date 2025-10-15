# XSparkyProject - Comprehensive Kotlin Library

## Overview
XSparkyProject is a powerful, all-in-one Kotlin library that combines 7 essential components for modern Kotlin development:

1. **Data Processing & Analytics** - DataFrame operations and data manipulation utilities
2. **Networking Utilities** - HTTP client with coroutine support
3. **Coroutines Extensions** - Advanced coroutine utilities and patterns
4. **Functional Programming Toolkit** - Functional types like Option, Either, and extension functions
5. **Validation & Serialization Framework** - Declarative validation DSL
6. **Microservice Toolkit** - Utilities for building microservices
7. **Security Framework** - Cryptographic utilities and security tools

## Features

### Data Processing
- DataFrame implementation for data manipulation
- Filter, map, and transformation operations
- Column and row access utilities

### Networking
- HTTP client with GET and POST support
- Coroutine-based asynchronous operations
- Response handling utilities

### Coroutines
- Retry mechanisms with exponential backoff
- Timeout handling with default values
- Parallel mapping with concurrency control

### Functional Programming
- Option and Either types for safe null handling
- Map and flatMap extension functions
- Functional composition utilities

### Validation
- Declarative validation DSL
- Built-in validation rules (NotNull, StringLength)
- Custom validation rule support

### Security
- Hash functions (SHA-256, MD5)
- AES encryption and decryption
- Key generation utilities

### Microservices
- Health check utilities
- Service registration and discovery helpers

## Installation

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>com.sparky</groupId>
    <artifactId>xsparkyproject</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or for Gradle:

```kotlin
implementation("com.sparky:xsparkyproject:1.0.0")
```

## Usage Examples

### Data Processing
```kotlin
val data = listOf(
    mapOf("name" to "Alice", "age" to 30),
    mapOf("name" to "Bob", "age" to 25)
)

val df = DataFrame.of(data)
val filtered = df.filter { it["age"] as Int > 25 }
```

### Networking
```kotlin
val response = HttpClient.get("https://api.example.com/data")
val postResponse = HttpClient.post("https://api.example.com/data", "{\"key\":\"value\"}")
```

### Functional Programming
```kotlin
val someValue = Some("Hello")
val noneValue = None

val result = someValue.getOrElse { "Default" } // Returns "Hello"
val noneResult = noneValue.getOrElse { "Default" } // Returns "Default"
```

### Validation
```kotlin
val validator = Validator.of<String>()
    .addRule(NotNullRule<String?>())
    .addRule(StringLengthRule(3, 10))
    .build()

val result = validator.validate("Hi") // Returns ValidationErrorList
```

## Author
Created by Sparky (Андрій Будильников)

## License
Apache License 2.0
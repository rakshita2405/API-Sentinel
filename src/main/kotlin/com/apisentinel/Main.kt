package com.apisentinel

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.util.Locale
import kotlin.system.measureTimeMillis

// Stores the result of one API test.
data class TestResult(
    val outcome: String,
    val responseTimeMs: Long
)

// The program starts here.
suspend fun main() {

    // Create the HTTP client and configure request timeouts.
    val client = HttpClient(CIO) {
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 20_000
            socketTimeoutMillis = 20_000
        }
    }

    // Count the results for the final report.
    var passed = 0
    var failed = 0
    var errors = 0

    try {
        println("================================")
        println("         API SENTINEL")
        println("================================")

        // STEP 1: Get the API URL from the user.
        print("Enter API URL: ")
        val url = readln().trim()

        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            println("Invalid URL. Use http:// or https://.")
            return
        }

        // STEP 2: Let the user choose an HTTP method.
        print("HTTP method (GET, POST, PUT, PATCH, DELETE): ")
        val methodInput = readln().trim().uppercase()

        val httpMethod = when (methodInput) {
            "GET" -> HttpMethod.Get
            "POST" -> HttpMethod.Post
            "PUT" -> HttpMethod.Put
            "PATCH" -> HttpMethod.Patch
            "DELETE" -> HttpMethod.Delete
            else -> {
                println("Invalid HTTP method.")
                return
            }
        }

        // STEP 3: Ask which HTTP status code is expected.
        print("Expected HTTP status (e.g. 200, 201, 204): ")
        val expectedStatus = readln().trim().toIntOrNull()

        if (expectedStatus == null || expectedStatus !in 100..599) {
            println("Invalid HTTP status. Enter a number from 100 to 599.")
            return
        }

        // STEP 4: Collect optional request headers.
        // Headers can contain Content-Type, an API key, or other metadata.
        print("Number of request headers (0 if none): ")
        val headerCount = readln().trim().toIntOrNull()

        if (headerCount == null || headerCount !in 0..50) {
            println("Enter a number of headers between 0 and 50.")
            return
        }

        val requestHeaders = mutableListOf<Pair<String, String>>()

        repeat(headerCount) {
            print("Header name: ")
            val headerName = readln().trim()

            print("Header value: ")
            val headerValue = readln()

            if (headerName.isBlank()) {
                println("Header name cannot be empty.")
                return
            }

            requestHeaders.add(headerName to headerValue)
        }

        // STEP 5: Collect an optional JSON request body.
        // This is commonly used with POST, PUT, and PATCH.
        println()
        println("Enter JSON request body on one line.")
        println("Press Enter without typing anything if no body is needed.")
        print("Request body: ")

        val requestBody = readln().trim()

        // Check that the request body is valid JSON before sending it.
        if (requestBody.isNotBlank()) {
            try {
                Json.parseToJsonElement(requestBody)
            } catch (e: Exception) {
                println("Invalid JSON request body: ${e.message}")
                return
            }
        }

        // STEP 6: Choose an optional JSON response assertion.
        println()
        println("JSON response assertion:")
        println("1. EXISTS   - Check whether a field exists")
        println("2. EQUALS   - Check whether a field matches a value")
        println("3. CONTAINS - Check whether a field contains text")
        println("4. SKIP     - Do not check a JSON field")

        print("Choose 1, 2, 3, or 4: ")
        val assertionChoice = readln().trim()

        if (assertionChoice !in listOf("1", "2", "3", "4")) {
            println("Invalid assertion choice.")
            return
        }

        var fieldName = ""
        var expectedValue = ""

        if (assertionChoice != "4") {
            print("JSON field name (e.g. name, email, username): ")
            fieldName = readln().trim()

            if (fieldName.isBlank()) {
                println("Field name cannot be empty.")
                return
            }

            // EXISTS only checks whether the field is present.
            if (assertionChoice == "2" || assertionChoice == "3") {
                print("Expected value: ")
                expectedValue = readln()
            }
        }

        println()
        println("Starting API test...")
        println("--------------------------------")

        // Run the API test using the inputs supplied by the user.
        val result = testApi(
            client = client,
            url = url,
            httpMethod = httpMethod,
            expectedStatus = expectedStatus,
            requestHeaders = requestHeaders,
            requestBody = requestBody,
            assertionChoice = assertionChoice,
            fieldName = fieldName,
            expectedValue = expectedValue
        )

        // Update the appropriate counter.
        when (result.outcome) {
            "PASS" -> passed++
            "FAIL" -> failed++
            "ERROR" -> errors++
        }

        // Calculate the total number of tests and the pass percentage.
        val total = passed + failed + errors

        val successRate = if (total > 0) {
            passed.toDouble() / total * 100
        } else {
            0.0
        }

        // Print the final report.
        println()
        println("================================")
        println("          TEST SUMMARY")
        println("================================")
        println("Total Tests: $total")
        println("Passed: $passed")
        println("Failed: $failed")
        println("Errors: $errors")
        println("Success Rate: ${
            "%.2f".format(Locale.US, successRate)
        }%")
        println("================================")

    } finally {
        // Always close the HTTP client, even if the program exits early.
        client.close()
    }
}


// Executes one API test and returns its result and response time.
suspend fun testApi(
    client: HttpClient,
    url: String,
    httpMethod: HttpMethod,
    expectedStatus: Int,
    requestHeaders: List<Pair<String, String>>,
    requestBody: String,
    assertionChoice: String,
    fieldName: String,
    expectedValue: String
): TestResult {

    try {
        println("Request method: ${httpMethod.value}")
        println("Request URL: $url")
        println("Expected status: $expectedStatus")

        var actualStatus = 0
        var responseBody = ""

        // Measure how long the HTTP request and response-body read take.
        val elapsedTime = measureTimeMillis {

            // Build and send the request using the selected method.
            val response = client.request(url) {

                method = httpMethod

                // Add the headers entered by the user.
                headers {
                    requestHeaders.forEach { (name, value) ->
                        append(name, value)
                    }

                    // Use JSON as the default content type when sending a body.
                    val hasContentType = requestHeaders.any {
                        it.first.equals("Content-Type", ignoreCase = true)
                    }

                    if (requestBody.isNotBlank() && !hasContentType) {
                        append(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
                    }
                }

                // Send the body only when the user supplied one.
                if (requestBody.isNotBlank()) {
                    setBody(requestBody)
                }
            }

            actualStatus = response.status.value
            responseBody = response.bodyAsText()
        }

        println("Actual status: $actualStatus")
        println("Response time: $elapsedTime ms")

        // Start by checking whether the returned status matches expectations.
        val statusMatches = actualStatus == expectedStatus

        println(
            "Status validation: ${if (statusMatches) "PASS" else "FAIL"}"
        )

        // A JSON assertion is optional.
        var fieldMatches = true

        if (assertionChoice != "4") {

            // Parse the response only when a JSON field needs to be checked.
            val jsonObject = try {
                Json.parseToJsonElement(responseBody).jsonObject
            } catch (e: Exception) {
                null
            }

            if (jsonObject == null) {
                fieldMatches = false
                println("JSON validation: FAIL")
                println("Reason: Response is not a valid JSON object.")
            } else {
                // Find the selected field in the top-level JSON object.
                val fieldElement = jsonObject[fieldName]

                // Convert the field value to text for comparison.
                val actualValue = when (fieldElement) {
                    null -> null
                    is JsonPrimitive -> {
                        if (fieldElement is JsonNull) {
                            "null"
                        } else {
                            fieldElement.content
                        }
                    }
                    else -> fieldElement.toString()
                }

                // Apply the assertion selected by the user.
                fieldMatches = when (assertionChoice) {

                    // The field must be present, even if its value is null.
                    "1" -> jsonObject.containsKey(fieldName)

                    // The field value must exactly match the expected text.
                    "2" -> actualValue == expectedValue

                    // The field value must contain the expected text.
                    "3" -> actualValue?.contains(expectedValue) == true

                    else -> true
                }

                println("Field checked: $fieldName")
                println("Actual value: ${actualValue ?: "FIELD NOT FOUND"}")

                if (assertionChoice == "2" || assertionChoice == "3") {
                    println("Expected value: $expectedValue")
                }

                println(
                    "JSON validation: ${if (fieldMatches) "PASS" else "FAIL"}"
                )
            }
        } else {
            println("JSON validation: SKIPPED")
        }

        // PASS requires the expected status and every selected assertion to pass.
        val outcome = if (statusMatches && fieldMatches) {
            "PASS"
        } else {
            "FAIL"
        }

        println()
        println("Response body:")
        println(if (responseBody.isBlank()) "(empty response body)" else responseBody)
        println()
        println("FINAL RESULT: $outcome")

        return TestResult(
            outcome = outcome,
            responseTimeMs = elapsedTime
        )

    } catch (e: Exception) {
        // A request failure or other unexpected execution problem is an ERROR.
        println()
        println("FINAL RESULT: ERROR")
        println("Reason: ${e.message ?: "Unknown error"}")

        return TestResult(
            outcome = "ERROR",
            responseTimeMs = 0
        )
    }
}
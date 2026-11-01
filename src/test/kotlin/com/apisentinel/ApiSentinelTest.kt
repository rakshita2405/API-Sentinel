package com.apisentinel

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTimedValue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int

object ApiConfig {
    const val BASE_URL = "https://jsonplaceholder.typicode.com"
}

class ApiSentinelTest {

    companion object {
        private lateinit var client: HttpClient
        private lateinit var api: ApiClient

        @JvmStatic
        @BeforeAll
        fun setup() {
            client = HttpClient(CIO)
            api = ApiClient(client)
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            client.close()
        }
    }

    @Test
    fun `existing user should return status 200 within five seconds`() = runBlocking {        val result = measureTimedValue {
        api.getUser(1)
    }

        val response = result.value
        val responseTime = result.duration

        assertEquals(200, response.status.value)

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertEquals("Leanne Graham", json["name"]?.jsonPrimitive?.content)
        println("API response time: $responseTime")

        assertTrue(
            responseTime < 5000.milliseconds,
            "API response exceeded 5000 ms: $responseTime"
        )
    }

    @Test
    fun `second user should return status 200`() = runBlocking {
        val response =
            api.getUser(2)

        assertEquals(200, response.status.value)

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertEquals("Ervin Howell", json["name"]?.jsonPrimitive?.content)
    }

    @Test
    fun `nonexistent user should return status 404`() = runBlocking {
        val response = api.getUser(999999)

        assertEquals(404, response.status.value)
    }

    @Test
    fun `creating a user should return status 201`() = runBlocking {
        val requestBody = """
        {
            "name": "Pranay",
            "username": "pranay123"
        }
    """.trimIndent()

        val response = api.createUser(requestBody)

        assertEquals(201, response.status.value)

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertEquals("Pranay", json["name"]?.jsonPrimitive?.content)
        assertEquals("pranay123", json["username"]?.jsonPrimitive?.content)
        assertEquals(11, json["id"]?.jsonPrimitive?.int)

        println("POST response status: ${response.status.value}")
        println("POST response body: $body")
    }

    @Test
    fun `updating a user should return status 200`() = runBlocking {
        val requestBody = """
            {
                "id": 1,
                "name": "Pranay Updated",
                "username": "pranay_updated"
            }
        """.trimIndent()

        val response = api.updateUser(1, requestBody)

        assertEquals(200, response.status.value)

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertEquals(1, json["id"]?.jsonPrimitive?.int)
        assertEquals("Pranay Updated", json["name"]?.jsonPrimitive?.content)
        assertEquals("pranay_updated", json["username"]?.jsonPrimitive?.content)

        println("PUT response status: ${response.status.value}")
        println("PUT response body: $body")
    }

    @Test
    fun `patching a user should return status 200`() = runBlocking {
        val requestBody = """
        {
            "name": "Pranay Patched"
        }
    """.trimIndent()

        val response = api.patchUser(1, requestBody)

        assertEquals(200, response.status.value)

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertEquals(1, json["id"]?.jsonPrimitive?.int)
        assertEquals("Pranay Patched", json["name"]?.jsonPrimitive?.content)

        println("PATCH response status: ${response.status.value}")
        println("PATCH response body: $body")
    }

    @Test
    fun `deleting a user should return status 200`() = runBlocking {
        val response = api.deleteUser(1)

        assertEquals(200, response.status.value)

        println("DELETE response status: ${response.status.value}")
        println("DELETE response body: ${response.bodyAsText()}")
    }
}

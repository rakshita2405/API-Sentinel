package com.apisentinel

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.statement.HttpResponse

class ApiClient(private val client: HttpClient) {

    suspend fun getUser(userId: Int): HttpResponse {
        return client.get("${ApiConfig.BASE_URL}/users/$userId")
    }

    suspend fun createUser(requestBody: String): HttpResponse {
        return client.post("${ApiConfig.BASE_URL}/users") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }

    suspend fun updateUser(userId: Int, requestBody: String): HttpResponse {
        return client.put("${ApiConfig.BASE_URL}/users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }

    suspend fun patchUser(userId: Int, requestBody: String): HttpResponse {
        return client.patch("${ApiConfig.BASE_URL}/users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }

    suspend fun deleteUser(userId: Int): HttpResponse {
        return client.delete("${ApiConfig.BASE_URL}/users/$userId")
    }
}


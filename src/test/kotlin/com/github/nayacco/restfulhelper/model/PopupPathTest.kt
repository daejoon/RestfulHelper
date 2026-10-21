package com.github.nayacco.restfulhelper.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PopupPathTest {

    @Test
    fun `sortNames - orders by url path ignoring case before http method`() {
        val names = listOf("GET /users", "DELETE /orders", "POST /Accounts", "GET /a params=x", "GET /a")

        val sorted = PopupPath.sortNames(names, "")

        assertEquals(listOf("GET /a", "GET /a params=x", "POST /Accounts", "DELETE /orders", "GET /users"), sorted)
    }

    @Test
    fun `sortNames - breaks case-insensitive path ties by case-sensitive comparison`() {
        val names = listOf("GET /users", "GET /Users")

        val sorted = PopupPath.sortNames(names, "")

        assertEquals(listOf("GET /Users", "GET /users"), sorted)
    }

    @Test
    fun `sortNames - orders same path by conventional http method order`() {
        val names = listOf("TRACE /a", "OPTIONS /a", "HEAD /a", "DELETE /a", "PATCH /a", "PUT /a", "POST /a", "GET /a", "CONNECT /a")

        val sorted = PopupPath.sortNames(names, "")

        assertEquals(
            listOf("GET /a", "POST /a", "PUT /a", "PATCH /a", "DELETE /a", "HEAD /a", "OPTIONS /a", "CONNECT /a", "TRACE /a"),
            sorted,
        )
    }
}

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
}

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

    @Test
    fun `sortNames - parses path and first method of multi-method names`() {
        val names = listOf("{DELETE, GET} /c", "POST /b", "PUT /a", "{POST, GET} /a")

        val sorted = PopupPath.sortNames(names, "")

        assertEquals(listOf("{POST, GET} /a", "PUT /a", "POST /b", "{DELETE, GET} /c"), sorted)
    }

    @Test
    fun `sortNames - places single method before multi-method with same first method`() {
        val names = listOf("{GET, POST} /a", "GET /a")

        val sorted = PopupPath.sortNames(names, "")

        assertEquals(listOf("GET /a", "{GET, POST} /a"), sorted)
    }

    @Test
    fun `sortNames - orders by match tier exact, prefix, substring, fuzzy ignoring case and leading slash`() {
        val names = listOf("GET /a/u-s-ers", "GET /admin/users", "GET /users/{id}", "POST /Users")
        val expected = listOf("POST /Users", "GET /users/{id}", "GET /admin/users", "GET /a/u-s-ers")

        assertEquals(expected, PopupPath.sortNames(names, "users"))
        assertEquals(expected, PopupPath.sortNames(names, "/USERS"))
    }

    @Test
    fun `sortNames - orders same match tier by higher matching degree first`() {
        // 둘 다 부분 일치지만 "/z/user"는 단어 시작에서 일치해 "/xuser"보다 점수가 높다.
        val names = listOf("GET /xuser", "GET /z/user")

        val sorted = PopupPath.sortNames(names, "user")

        assertEquals(listOf("GET /z/user", "GET /xuser"), sorted)
    }

    @Test
    fun `sortNames - ignores match accuracy for empty or slash-only pattern`() {
        // "-a/"는 '/'가 맨 앞이 아니어서 '/' 검색어 매칭 점수가 "/z"보다 낮지만, 경로명 순으로는 앞이다.
        val names = listOf("GET /z", "GET -a/", "GET /b")
        val expected = listOf("GET -a/", "GET /b", "GET /z")

        assertEquals(expected, PopupPath.sortNames(names, ""))
        assertEquals(expected, PopupPath.sortNames(names, "/"))
    }

    @Test
    fun `sortNames - collapses repeated curly brackets in pattern before matching`() {
        val names = listOf("GET /a/users/{id}", "GET /users/{id}")

        val sorted = PopupPath.sortNames(names, "/users/{{id}}")

        assertEquals(listOf("GET /users/{id}", "GET /a/users/{id}"), sorted)
    }

    @Test
    fun `sortNames - keeps sorting names without space or closing bracket without exception`() {
        val names = listOf("{GET", "GET", "GET /a")

        val sorted = PopupPath.sortNames(names, "a")

        assertEquals(listOf("GET /a", "GET", "{GET"), sorted)
    }
}

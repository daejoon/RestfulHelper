package com.github.nayacco.restfulhelper.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CommonUtilsTest {

    @Test
    fun `collapseRepeatedCurlyBrackets - collapses consecutive opening brackets into one`() {
        assertEquals("{var}", "{{var}".collapseRepeatedCurlyBrackets())
        assertEquals("{var}", "{{{var}".collapseRepeatedCurlyBrackets())
    }

    @Test
    fun `collapseRepeatedCurlyBrackets - collapses consecutive closing brackets into one`() {
        assertEquals("{var}", "{var}}".collapseRepeatedCurlyBrackets())
        assertEquals("{var}", "{var}}}".collapseRepeatedCurlyBrackets())
    }

    @Test
    fun `collapseRepeatedCurlyBrackets - normalizes double brackets on both sides`() {
        assertEquals("{pricingId}", "{{pricingId}}".collapseRepeatedCurlyBrackets())
        assertEquals(
            "/v1/seller/voucher/redeem/status/{pricingId}",
            "/v1/seller/voucher/redeem/status/{{pricingId}}".collapseRepeatedCurlyBrackets(),
        )
    }

    @Test
    fun `collapseRepeatedCurlyBrackets - normalizes asymmetric bracket counts independently`() {
        assertEquals("{var}", "{{{var}}".collapseRepeatedCurlyBrackets())
        assertEquals("{var}", "{{var}}}".collapseRepeatedCurlyBrackets())
    }

    @Test
    fun `collapseRepeatedCurlyBrackets - leaves single brackets unchanged`() {
        assertEquals("{var}", "{var}".collapseRepeatedCurlyBrackets())
        assertEquals(
            "/v1/seller/voucher/redeem/status/{pricingId}",
            "/v1/seller/voucher/redeem/status/{pricingId}".collapseRepeatedCurlyBrackets(),
        )
    }

    @Test
    fun `collapseRepeatedCurlyBrackets - returns input without brackets unchanged`() {
        assertEquals("", "".collapseRepeatedCurlyBrackets())
        assertEquals("/v1/seller/voucher", "/v1/seller/voucher".collapseRepeatedCurlyBrackets())
        assertEquals("/", "/".collapseRepeatedCurlyBrackets())
    }

    @Test
    fun `collapseRepeatedCurlyBrackets - normalizes multiple path variables in a path`() {
        assertEquals(
            "/a/{x}/b/{y}",
            "/a/{{x}}/b/{{y}}".collapseRepeatedCurlyBrackets(),
        )
    }
}

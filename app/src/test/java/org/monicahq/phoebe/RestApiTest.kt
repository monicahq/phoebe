package org.monicahq.phoebe

import org.junit.Test
import org.junit.Assert.*
import org.monicahq.phoebe.api.RestApi

class RestApiTest {

    @Test
    fun constructTest() {
        var restApi = RestApi.getApi("http://test")
        assertEquals(RestApi::class.java, restApi.javaClass)
    }

    @Test
    fun addFinalSlash() {
        var restApi = RestApi.getApi("http://test")
        assertEquals("http://test/", restApi.Url)
    }
}

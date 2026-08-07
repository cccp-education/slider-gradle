package slider.config

import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class YamlMapperFactoryTest {

    @Test
    fun `create returns a configured ObjectMapper`() {
        val mapper = YamlMapperFactory.create()
        assertNotNull(mapper)
    }

    @Test
    fun `create reads a simple YAML mapping as a Map`() {
        val mapper = YamlMapperFactory.create()
        val yaml = "name: slider\nversion: 0.0.11\n"
        val mapType = mapper.typeFactory
            .constructMapType(Map::class.java, String::class.java, String::class.java)
        @Suppress("UNCHECKED_CAST")
        val parsed = mapper.readValue(yaml, mapType) as Map<String, String>
        assertEquals("slider", parsed["name"])
        assertEquals("0.0.11", parsed["version"])
    }

    @Test
    fun `create supports Kotlin data classes`() {
        data class Person(val name: String, val age: Int)

        val mapper = YamlMapperFactory.create()
        val yaml = "name: Alice\nage: 30\n"
        val person = mapper.readValue(yaml, Person::class.java)
        assertEquals("Alice", person.name)
        assertEquals(30, person.age)
    }

    @Test
    fun `create disables WRITE_DATES_AS_TIMESTAMPS`() {
        val mapper = YamlMapperFactory.create()
        val enabled = mapper.serializationConfig
            .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        assertFalse(enabled)
    }

    @Test
    fun `two create calls produce independent mappers`() {
        val a = YamlMapperFactory.create()
        val b = YamlMapperFactory.create()
        assertTrue(a !== b)
    }
}
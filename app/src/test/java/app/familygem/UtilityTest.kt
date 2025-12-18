package app.familygem

import org.folg.gedcom.model.Person
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for utility functions.
 */
class UtilityTest {

    @Test
    fun properNameExtractsNameCorrectly() {
        val person = Person()
        val personName = org.folg.gedcom.model.Name()
        personName.value = "Иван Петрович/Иванов/"
        person.addName(personName)
        
        val name = U.properName(person)
        
        assertNotNull(name)
        assertTrue(name.contains("Иван"))
        assertTrue(name.contains("Иванов"))
    }

    @Test
    fun properNameHandlesEmptyName() {
        val person = Person()
        val personName = org.folg.gedcom.model.Name()
        personName.value = ""
        person.addName(personName)
        
        val name = U.properName(person)
        
        assertNotNull(name)
    }

    @Test
    fun properNameHandlesNullPerson() {
        val name = U.properName(null)
        
        assertNotNull(name)
        assertEquals("", name)
    }

    @Test
    fun twoDatesExtractsBirthAndDeath() {
        val person = Person()
        
        val birth = org.folg.gedcom.model.EventFact()
        birth.tag = "BIRT"
        birth.date = "1 JAN 1980"
        person.addEventFact(birth)
        
        val death = org.folg.gedcom.model.EventFact()
        death.tag = "DEAT"
        death.date = "31 DEC 2050"
        person.addEventFact(death)
        
        val dates = U.twoDates(person, false)
        
        assertNotNull(dates)
        assertTrue(dates.isNotEmpty())
    }

    @Test
    fun twoDatesHandlesOnlyBirth() {
        val person = Person()
        
        val birth = org.folg.gedcom.model.EventFact()
        birth.tag = "BIRT"
        birth.date = "15 MAR 1990"
        person.addEventFact(birth)
        
        val dates = U.twoDates(person, false)
        
        assertNotNull(dates)
        assertTrue(dates.isNotEmpty())
    }

    @Test
    fun findRootIdReturnsFirstPersonWhenNoFamilies() {
        val gedcom = org.folg.gedcom.model.Gedcom()
        
        val person = Person()
        person.id = "I1"
        gedcom.addPerson(person)
        
        val rootId = U.findRootId(gedcom)
        
        assertEquals("I1", rootId)
    }

    @Test
    fun findRootIdReturnsNullForEmptyGedcom() {
        val gedcom = org.folg.gedcom.model.Gedcom()
        
        val rootId = U.findRootId(gedcom)
        
        assertNull(rootId)
    }
}

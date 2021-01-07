package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class DataDBTests_05_FindByID : DataDBTests() {

    class LDB : DataDBTests_05_FindByID() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_05_FindByID() { override val factory = DataDB.inMemory }


    @Test
    fun test00_FindByPKCompositeKey() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa", "a")), Value.ofAscii("ValueAa1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa", "b")), Value.ofAscii("ValueAb1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), mapOf("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findById(1, Value.ofAscii("aaa")).use {
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0, 'a', 0), byteArray("ValueAa1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0, 'b', 0), byteArray("ValueAb1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindByPKReverseCompositeKey() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa", "a")), Value.ofAscii("ValueAa1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa", "b")), Value.ofAscii("ValueAb1!"), mapOf("Symbols" to Value.ofAscii("gamma", "delta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), mapOf("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findById(1, Value.ofAscii("aaa")).use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0, 'b', 0), byteArray("ValueAb1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0, 'a', 0), byteArray("ValueAa1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test02_FindByPKUnknownKey() {
        ddb.put( ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put( ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), mapOf("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findById(1, Value.ofAscii("ccc")).use {
            assertFalse(it.isValid())
        }
    }
}

package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class DataDBTests_10_CloseOpen : DataDBTests() {

    class LDB : DataDBTests_10_CloseOpen() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_10_CloseOpen() { override val factory = DataDB.inMemory }


    @Test
    fun test090_PutCloseOpenGet() {
        ddb.put(ddb.newKey(1, Value.of("key")), Value.of("value"))

        ddb.close()

        open()

        val key = ddb.newKey(1, Value.of("key"))
        ddb.get(key)!!.use {
            assertBytesEquals(array("value"), it)
        }
    }

    @Test
    fun test091_PutCloseOpenIter() {
        ddb.put(ddb.newKey(1, Value.of("key")), Value.of("value"))

        ddb.close()

        open()

        val it = ddb.findAllByType(1)
        try {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "key", 0), array("value"), it)

            it.next()
            assertFalse(it.isValid())
        } finally {
            it.close()
        }
    }

}

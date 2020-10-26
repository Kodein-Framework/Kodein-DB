package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.default
import org.kodein.db.leveldb.inDir
import org.kodein.db.leveldb.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class LDBTests_02_Batch : LevelDBTests() {

    class LDB : LDBTests_02_Batch() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_02_Batch() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_PutGet() {
        ldb!!.newWriteBatch().use { batch ->
            batch.put(buffer(1), buffer("one"))
            batch.put(buffer(2), buffer("two"))

            assertNull(ldb!!.get(buffer(1)))
            assertNull(ldb!!.get(buffer(2)))

            ldb!!.write(batch)
        }

        val value1 = ldb!!.get(buffer(1))!!
        val value2 = ldb!!.get(buffer(2))!!
        assertBytesEquals(byteArray("one"), value1)
        assertBytesEquals(byteArray("two"), value2)
        value1.close()
        value2.close()
    }

    @Test
    fun test_01_DirectPutGet() {
        ldb!!.newWriteBatch().use { batch ->
            batch.put(buffer(1), buffer("one"))
            batch.put(buffer(2), buffer("two"))
            batch.put(buffer(3), buffer("three"))

            assertNull(ldb!!.get(buffer(1)))
            assertNull(ldb!!.get(buffer(2)))
            assertNull(ldb!!.get(buffer(3)))

            ldb!!.write(batch, LevelDB.WriteOptions(sync = true))
        }

        val value1 = ldb!!.get(buffer(1))!!
        val value2 = ldb!!.get(buffer(2))!!
        val value3 = ldb!!.get(buffer(3))!!
        assertBytesEquals(byteArray("one"), value1)
        assertBytesEquals(byteArray("two"), value2)
        assertBytesEquals(byteArray("three"), value3)
        value1.close()
        value2.close()
        value3.close()
    }

    @Test
    fun test_02_DeleteGet() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(2), buffer("two"))

        ldb!!.newWriteBatch().use { batch ->
            batch.delete(buffer(1))
            batch.delete(buffer(2))

            val value1 = ldb!!.get(buffer(1))!!
            val value2 = ldb!!.get(buffer(2))!!
            assertBytesEquals(byteArray("one"), value1)
            assertBytesEquals(byteArray("two"), value2)
            value1.close()
            value2.close()

            ldb!!.write(batch)
        }

        assertNull(ldb!!.get(buffer(1)))
        assertNull(ldb!!.get(buffer(2)))
    }

    @Test
    fun test_03_DirectDeleteGet() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(2), buffer("two"))

        ldb!!.newWriteBatch().use { batch ->
            batch.delete(buffer(1))
            batch.delete(buffer(2))

            val value1 = ldb!!.get(buffer(1))!!
            val value2 = ldb!!.get(buffer(2))!!
            assertBytesEquals(byteArray("one"), value1)
            assertBytesEquals(byteArray("two"), value2)
            value1.close()
            value2.close()

            ldb!!.write(batch)
        }

        assertNull(ldb!!.get(buffer(1)))
        assertNull(ldb!!.get(buffer(2)))
    }


}
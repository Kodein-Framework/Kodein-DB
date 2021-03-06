package org.kodein.db.impl.model.cache

import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.impl.model.default
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.delete
import org.kodein.db.model.get
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("ClassName")
abstract class CacheDBTests_02_Batch : CacheDBTests() {

    class LDB : CacheDBTests_02_Batch() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : CacheDBTests_02_Batch() { override val factory = ModelDB.inMemory }


    @Test
    fun test00_Put() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)

        mdb.newBatch().use { batch ->
            batch.put(me)

            assertNull(mdb[key])

            MaybeThrowable().also { batch.write(it) } .shoot()
        }

        assertSame(me, mdb[key]!!.model)
    }

    @Test
    fun test01_Delete() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)
        mdb.put(key, me)

        mdb.newBatch().use { batch ->
            batch.delete(key)

            assertNotNull(mdb[key])

            MaybeThrowable().also { batch.write(it) } .shoot()
        }

        assertNull(mdb[key])
    }
}

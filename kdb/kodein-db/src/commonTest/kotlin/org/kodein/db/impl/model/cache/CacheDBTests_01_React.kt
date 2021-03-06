package org.kodein.db.impl.model.cache

import org.kodein.db.DBListener
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.impl.model.default
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.ModelDBListener
import org.kodein.db.model.delete
import org.kodein.db.model.get
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.ReadMemory
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("ClassName")
abstract class CacheDBTests_01_React : CacheDBTests() {

    class LDB : CacheDBTests_01_React() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : CacheDBTests_01_React() { override val factory = ModelDB.inMemory }


    @Test
    fun test00_ReactDidPutException() {
        val listener = object : ModelDBListener<Any> {
            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) = throw IllegalStateException()
        }

        mdb.register(listener)

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))

        assertFailsWith<IllegalStateException> {
            mdb.put(me)
        }

        assertSame(me, mdb[mdb.keyFrom(me)]!!.model)
    }

    @Test
    fun test01_ReactDidDeleteException() {
        val listener = object : ModelDBListener<Any> {
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) = throw IllegalStateException()
        }

        mdb.register(listener)

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)
        mdb.put(me)

        assertFailsWith<IllegalStateException> {
            mdb.delete(key)
        }

        assertNull(mdb[key])

    }

}

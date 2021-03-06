package org.kodein.db.impl.data

import org.kodein.db.Anticipate
import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class DataDBTests_12_Checks : DataDBTests() {

    class LDB : DataDBTests_12_Checks() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_12_Checks() { override val factory = DataDB.inMemory }


    @Test
    fun test00_putOK() {
        val key = ddb.newKey(1, Value.of("test"))

        ddb.put(key, Value.of(21))
        ddb.put(key, Value.of(42), emptyMap(), Anticipate {
            ddb.get(key)!!.use {
                check(it.getInt(0) == 21)
            }
        })

        ddb.get(key)!!.use {
            assertEquals(42, it.getInt(0))
        }
    }

    @Test
    fun test01_putKO() {
        val key = ddb.newKey(1, Value.of("test"))

        ddb.put(key, Value.of(21))
        assertFailsWith<IllegalStateException> {
            ddb.put(key, Value.of(42), emptyMap(), Anticipate {
                ddb.get(key)!!.use {
                    check(it.getInt(0) == 0)
                }
            })
        }

        ddb.get(key)!!.use {
            assertEquals(21, it.getInt(0))
        }
    }

    @Test
    fun test02_deleteOK() {
        val key = ddb.newKey(1, Value.of("test"))
        ddb.put(key, Value.of(42))

        ddb.delete(key, Anticipate {
            ddb.get(key)!!.use {
                check(it.getInt(0) == 42)
            }
        })

        assertNull(ddb.get(key))
    }

    @Test
    fun test03_deleteKO() {
        val key = ddb.newKey(1, Value.of("test"))
        ddb.put(key, Value.of(42))

        assertFailsWith<IllegalStateException> {
            ddb.delete(key, Anticipate {
                ddb.get(ddb.newKey(1, Value.of("test")))!!.use {
                    check(it.getInt(0) == 0)
                }
            })
        }

        ddb.get(key)!!.use {
            assertEquals(42, it.getInt(0))
        }
    }

    @Test
    fun test04_batchOK() {
        val key = ddb.newKey(1, Value.of("test"))
        ddb.put(key, Value.of(21))

        ddb.newBatch().use { batch ->
            batch.put(key, Value.of(42))
            MaybeThrowable().also {
                batch.write(it, Anticipate {
                    ddb.get(key)!!.use {
                        check(it.getInt(0) == 21)
                    }
                })
            }.shoot()
        }

        ddb.get(key)!!.use {
            assertEquals(42, it.getInt(0))
        }
    }

    @Test
    fun test05_batchKO() {
        val key = ddb.newKey(1, Value.of("test"))
        ddb.put(key, Value.of(21))

        ddb.newBatch().use { batch ->
            batch.put(key, Value.of(42))
            assertFailsWith<IllegalStateException> {
                MaybeThrowable().also {
                    batch.write(it, Anticipate {
                        ddb.get(key)!!.use {
                            check(it.getInt(0) == 0)
                        }
                    })
                }.shoot()
            }
        }

        ddb.get(key)!!.use {
            assertEquals(21, it.getInt(0))
        }
    }

}

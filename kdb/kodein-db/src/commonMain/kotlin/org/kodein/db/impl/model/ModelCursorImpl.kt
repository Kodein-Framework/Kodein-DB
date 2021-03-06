package org.kodein.db.impl.model

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.data.DataCursor
import org.kodein.db.impl.data.getDocumentKeyID
import org.kodein.db.model.ModelCursor
import org.kodein.memory.Closeable
import org.kodein.memory.io.Memory
import org.kodein.memory.io.getBytes
import org.kodein.memory.io.wrap
import kotlin.reflect.KClass

internal open class ModelCursorImpl<M : Any>(override val cursor: DataCursor, val mdb: ModelDBImpl, private val modelType: KClass<M>) : ModelCursor<M>, ResettableCursorModule, Closeable by cursor {

    private var key: Key<M>? = null
    private var model: Sized<M>? = null

    override fun reset() {
        key = null
        model = null
    }

    override fun key() = key ?: Key<M>(Memory.wrap(cursor.transientKey().getBytes())).also { key = it }

    override fun model(vararg options: Options.Get): Sized<M> = model ?: mdb.deserialize(modelType, getDocumentKeyID(key().bytes), cursor.transientValue(), options).also { model = it }

}

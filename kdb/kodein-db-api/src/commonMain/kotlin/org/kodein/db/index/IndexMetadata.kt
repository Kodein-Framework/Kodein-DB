package org.kodein.db.index

import org.kodein.db.model.orm.Metadata

public abstract class IndexMetadata : Metadata {
    public abstract fun modelIndex(): ModelIndex<*>

    private val modelIndex by lazy { modelIndex().indexes.associate { it.pair } }

    final override fun indexes(): Map<String, Any> = modelIndex
}

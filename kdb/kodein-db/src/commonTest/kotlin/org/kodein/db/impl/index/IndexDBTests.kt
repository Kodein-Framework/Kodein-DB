package org.kodein.db.impl.index

import org.kodein.db.DBWrite
import org.kodein.db.impl.DBTests

abstract class IndexDBTests : DBTests() {
    object IndexModels {
        val berlin = IndexCity("Berlin", "Germany", 19115)
        val paris = IndexCity("Paris", "France", 75000)
        val dresden = IndexCity("Dresden", "Germany", 1067)
    }

    fun DBWrite.inflateIndexDB() {
        put(IndexModels.berlin)
        put(IndexModels.paris)
        put(IndexModels.dresden)
    }
}

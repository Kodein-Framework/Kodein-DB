package org.kodein.db.impl.index

import org.kodein.db.impl.DBTests
import org.kodein.db.index.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class IndexDBTests_00_Find : IndexDBTests() {
    class LDB : IndexDBTests_00_Find(), DBTests.LDB
    class IM : IndexDBTests_00_Find(), DBTests.IM

    @Test
    fun test00_anyAndNoneFilter() {
        db.inflateIndexDB()

        assertTrue(db.any(IndexCity.Index::name eq "Berlin"))
        assertTrue(db.none(IndexCity.Index::name eq "Kopenhagen"))
    }

    @Test
    fun test01_findModelListFilter() {
        db.inflateIndexDB()

        assertEquals(db.findModelList(IndexCity.Index::country eq "Germany"), listOf(IndexModels.berlin, IndexModels.dresden))
    }

    @Test
    fun test02_findFilter_overTriple() {
        db.inflateIndexDB()

        assertEquals(db.findOneOrNull(IndexCity.Index::nameCountryPostalCode eq Triple("Paris", "France", 75000)), IndexModels.paris)
    }
}

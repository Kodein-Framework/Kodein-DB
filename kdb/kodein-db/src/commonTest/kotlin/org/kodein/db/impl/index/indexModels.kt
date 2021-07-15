package org.kodein.db.impl.index

import kotlinx.serialization.Serializable
import org.kodein.db.index.IndexMetadata
import org.kodein.db.index.ModelIndex

@Serializable
data class IndexCity(val name: String, val country: String, val postalCode: Int) : IndexMetadata() {
    override val id get() = postalCode

    override fun modelIndex() = Index(this)

    class Index(override val model: IndexCity) : ModelIndex<IndexCity>() {
        val name by index { name }
        val country by index { country }

        val nameCountryPostalCode by indexTriple { Triple(name, country, postalCode) }
    }
}

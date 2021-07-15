package org.kodein.db.index

import org.kodein.db.DB

/**
 * Deletes all elements in this database matching the given [filter].
 */
public inline fun <reified M : Any> DB.delete(filter: Filter<M>) {
    filter.delete(this)
}

package org.kodein.db.index

import org.kodein.db.Cursor
import org.kodein.db.DBRead
import org.kodein.db.useModels
import org.kodein.memory.use

/**
 * Returns a [Cursor] where all elements match the
 * given [filter].
 */
public fun <M : Any> DBRead.find(filter: Filter<M>): Cursor<M> = filter.find(this)

/**
 * Checks if at least one element in the database matches
 * the given filter.
 *
 * @return true, if one element matches the [filter]
 */
public fun DBRead.any(filter: Filter<*>): Boolean = filter.find(this).use { it.isValid() }

/**
 * Checks if no element in the database matches the given
 * filter.
 *
 * @return true, if no element matches the [filter] or if there
 * are no elements in the database
 */
public fun DBRead.none(filter: Filter<*>): Boolean = filter.find(this).use { !it.isValid() }

/**
 * Returns a [List] containing all elements in the database
 * matching the given [filter].
 */
public fun <M : Any> DBRead.findModelList(filter: Filter<M>): List<M> = filter.find(this).useModels { it.toList() }

/**
 * Returns the first element in the database matching the
 * given [filter].
 *
 * Please note: this function does not ensure that such a model actually exists
 */
public fun <M : Any> DBRead.findOne(filter: Filter<M>): M = filter.find(this).use { it.model() }

/**
 * Returns the first element in the database matching the
 * given [filter] or null of there is no such element.
 */
public fun <M : Any> DBRead.findOneOrNull(filter: Filter<M>): M? =
    filter.find(this).use { if (it.isValid()) it.model() else null }

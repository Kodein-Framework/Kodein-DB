package org.kodein.db.index

import org.kodein.db.*
import kotlin.reflect.KProperty1

public abstract class Filter<M : Any> {
    /**
     * Returns a [Cursor] with all elements in the given database matching
     * this filter.
     */
    public abstract fun find(db: DBRead): Cursor<M>
}

/**
 * Deletes all elements in the given database matching this filter.
 */
public inline fun <reified M : Any> Filter<M>.delete(db: DB) {
    db.deleteAll(find(db))
}

/**
 * Creates a new filter matching all elements where the
 * content of the index equals the given [value].
 */
public inline infix fun <reified M : Any, T : Any> KProperty1<*, IndexSingleDefinition<M, T>>.eq(value: T): Filter<M> =
    object : Filter<M>() {
        override fun find(db: DBRead) = db.find<M>().byIndex(this@eq.name, value)
    }

/**
 * Creates a new filter matching all elements where the content
 * of the index equals the given [pair] of values.
 */
public inline infix fun <reified M : Any, A : Any, B : Any> KProperty1<*, IndexPairDefinition<M, A, B>>.eq(pair: Pair<A, B>): Filter<M> =
    object : Filter<M>() {
        override fun find(db: DBRead) = db.find<M>().byIndex(this@eq.name, pair.first, pair.second)
    }

/**
 * Creates a new filter matching all elements where the content
 * of the index equals the given [triple] of values.
 */
public inline infix fun <reified M : Any, A : Any, B : Any, C : Any> KProperty1<*, IndexTripleDefinition<M, A, B, C>>.eq(triple: Triple<A, B, C>): Filter<M> =
    object : Filter<M>() {
        override fun find(db: DBRead) = db.find<M>().byIndex(this@eq.name, triple.first, triple.second, triple.third)
    }

/**
 * Creates a new filter matching all elements where the content
 * of the composite index matches the given composite index values.
 *
 * Note: the array does not have to satisfy all values of the composite index,
 * it may be incomplete
 *
 * Attention: creating this filter is not typesafe, please make sure that all
 * the given types are valid (the same as the types of the properties of the
 * composite index)
 */
public inline infix fun <reified M : Any> KProperty1<*, IndexCompositeDefinition<M>>.eq(values: Array<Any>): Filter<M> =
    object : Filter<M>() {
        override fun find(db: DBRead) = db.find<M>().byIndex(this@eq.name, *values)
    }

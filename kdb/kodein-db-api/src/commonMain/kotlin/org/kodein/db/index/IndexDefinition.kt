package org.kodein.db.index

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * Classes which inherit from this abstract class can be used to define indexes
 * for model.
 * These classes should be defined inside the other class.
 */
public abstract class ModelIndex<M> {
    public abstract val model: M

    @PublishedApi
    internal val indexes: ArrayList<IndexDefinition<M>> = ArrayList()

    /**
     * Creates a new single index. (Index over one property)
     *
     * The name of the index is the name of its property.
     *
     * Usage:
     * ```kt
     * val name by index { name }
     * ```
     */
    public inline fun <T : Any> index(
        crossinline valueProvider: M.() -> T
    ): PropertyDelegateProvider<ModelIndex<M>, ReadOnlyProperty<ModelIndex<M>, IndexSingleDefinition<M, T>>> =
        PropertyDelegateProvider { _, property ->
            val indexDef = IndexSingleDefinition<M, T>(property.name, valueProvider.invoke(model))
                .apply { indexes += this }
            ReadOnlyProperty { _, _ -> indexDef }
        }

    /**
     * Creates a new pair index. (Index over two related properties)
     *
     * The name of the index is the name of its property.
     *
     * Usage:
     * ```kt
     * val name by indexPair { firstName to lastName }
     * ```
     */
    public inline fun <A : Any, B : Any> indexPair(
        crossinline valueProvider: M.() -> Pair<A, B>,
    ): PropertyDelegateProvider<ModelIndex<M>, ReadOnlyProperty<ModelIndex<M>, IndexPairDefinition<M, A, B>>> =
        PropertyDelegateProvider { _, property ->
            val pair = valueProvider.invoke(model)
            val indexDef = IndexPairDefinition<M, A, B>(property.name, pair.first, pair.second)
                .apply { indexes += this }
            ReadOnlyProperty { _, _ -> indexDef }
        }

    /**
     * Creates a new triple index. (Index over three related properties)
     *
     * The name of the index is the name of its property.
     *
     * Usage:
     * ```kt
     * val name by indexTriple { Triple(firstName, secondName, lastName) }
     * ```
     */
    public inline fun <A : Any, B : Any, C : Any> indexTriple(
        crossinline valueProvider: M.() -> Triple<A, B, C>,
    ): PropertyDelegateProvider<ModelIndex<M>, ReadOnlyProperty<ModelIndex<M>, IndexTripleDefinition<M, A, B, C>>> =
        PropertyDelegateProvider { _, property ->
            val triple = valueProvider.invoke(model)
            val indexDef = IndexTripleDefinition<M, A, B, C>(property.name, triple.first, triple.second, triple.third)
                .apply { indexes += this }
            ReadOnlyProperty { _, _ -> indexDef }
        }

    /**
     * Creates a new composite index.
     *
     * The name of the index is the name of its property.
     *
     * Usage:
     * ```kt
     * val name by indexComposite { arrayOf(firstName, middleName, thirdName, lastName) }
     * ```
     */
    public inline fun <T : Any> indexComposite(
        crossinline valueProvider: M.() -> Array<T>
    ): PropertyDelegateProvider<ModelIndex<M>, ReadOnlyProperty<ModelIndex<M>, IndexCompositeDefinition<M>>> =
        PropertyDelegateProvider { _, property ->
            val indexDef = IndexCompositeDefinition<M>(property.name, valueProvider.invoke(model))
                .apply { indexes += this }
            ReadOnlyProperty { _, _ -> indexDef }
        }
}

public interface IndexDefinition<M> {
    public val pair: Pair<String, Any>
}

public class IndexSingleDefinition<M, T : Any>(name: String, value: T) : IndexDefinition<M> {
    override val pair: Pair<String, T> = name to value
}

public class IndexPairDefinition<M, A : Any, B : Any>(name: String, first: A, second: B) : IndexDefinition<M> {
    override val pair: Pair<String, Array<Any>> = name to arrayOf(first, second)
}

public class IndexTripleDefinition<M, A : Any, B : Any, C : Any>(name: String, first: A, second: B, third: C) : IndexDefinition<M> {
    override val pair: Pair<String, Array<Any>> = name to arrayOf(first, second, third)
}

public class IndexCompositeDefinition<M>(name: String, values: Array<out Any>) : IndexDefinition<M> {
    override val pair: Pair<String, Array<out Any>> = name to values
}

package me.camsteffen.polite.util

import java.util.PriorityQueue

fun <T : Comparable<T>> Collection<Sequence<T>>.mergeSorted(): Sequence<T> {
    return mergeSortedWith(naturalOrder())
}

inline fun <T : Any> Collection<Sequence<T>>.mergeSortedBy(
    crossinline selector: (T) -> Comparable<*>
): Sequence<T> {
    return mergeSortedWith(compareBy(selector))
}

fun <T : Any> Collection<Sequence<T>>.mergeSortedWith(comparator: Comparator<in T>): Sequence<T> {
    if (isEmpty()) return emptySequence()
    if (size == 1) return iterator().next()

    val queue: PriorityQueue<IterItemPair<T>> = asSequence()
        .map { it.iterator() }
        .filter { it.hasNext() }
        .mapTo(
            PriorityQueue<IterItemPair<T>>(size, compareBy(comparator) { it.item })
        ) { IterItemPair(it, it.next()) }

    return sequence {
        while (true) {
            val pair = queue.poll() ?: break
            yield(pair.item)
            if (pair.iterator.hasNext()) {
                val next = pair.iterator.next()
                check(comparator.compare(pair.item, next) <= 0) { "$next is out of order" }
                pair.item = next
                queue.add(pair)
            }
        }
    }
}

private data class IterItemPair<T>(val iterator: Iterator<T>, var item: T)

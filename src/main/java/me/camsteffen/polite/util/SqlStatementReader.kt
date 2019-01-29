package me.camsteffen.polite.util

import java.io.Closeable
import java.io.Reader

/**
 * A simple SQL parser for iterating statements and skipping comments
 */
class SqlStatementReader(private val reader: Reader) : AbstractIterator<String>(), Closeable {

    private var i: Int = 0

    override fun computeNext() {
        val statement = nextStatement()
        if (statement == null) {
            done()
        } else {
            setNext(statement)
        }
    }

    override fun close() {
        reader.close()
    }

    private fun nextStatement(): String? {
        while (true) {
            nextChar()
            if (end()) return null
            val c = i.toChar()
            if (c.isWhitespace()) continue
            when (c) {
                ';' -> {}
                '-' -> singleLineComment()
                '/' -> multiLineComment()
                else -> return statement()
            }
        }
    }

    private fun nextChar() {
        i = reader.read()
    }

    private fun accept(c: Char): Boolean {
        return if (i.toChar() == c) {
            nextChar()
            true
        } else {
            false
        }
    }

    private fun expect(c: Char) {
        require(i.toChar() == c)
        nextChar()
    }

    private fun end(): Boolean = i == -1

    private fun statement(): String {
        val builder = StringBuilder()
        while (!(end() || accept(';'))) {
            builder.append(i.toChar())
            nextChar()
        }
        return builder.toString()
    }

    private fun singleLineComment() {
        expect('-')
        expect('-')
        while (!end() && i.toChar() != '\n') {
            nextChar()
        }
    }

    private fun multiLineComment() {
        expect('/')
        expect('*')
        while (!(end() || accept('*') && accept('/'))) {
            nextChar()
        }
    }
}

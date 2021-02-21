package pt.restricoes.services

import java.lang.Exception

object U {

    fun <A> safe(f: () -> A): A? {
        return try { f() } catch (e: Exception) { null }
    }
}
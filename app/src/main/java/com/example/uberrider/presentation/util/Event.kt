package com.example.uberrider.presentation.util

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set //Allow external read but not write

    //Return the content and prevents its use again
    fun getContentIfNotHandled(): T?{
        return if(hasBeenHandled){
            null
        }else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
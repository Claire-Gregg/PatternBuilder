package com.example.pattern_creator.model

import android.graphics.Color
import androidx.annotation.ColorInt

class Colour constructor(@ColorInt private var _colour: Int) {
    @ColorInt
    val colour: Int = _colour
    private var _name: String = "#" + Color.red(_colour) + Color.green(_colour) + Color.blue(_colour)
    val name: String
        get() = _name
    private var _checked = true
    val checked: Boolean
        get() = _checked

    private var _numberOfUses = 0
    val numberOfUses: Int
        get() = _numberOfUses

    fun setName(newName: String) {
        _name = newName
    }

    fun addUse(){
        _numberOfUses++
    }

    fun resetUses(){
        _numberOfUses = 0
    }

    fun check() {
        _checked = !_checked
    }

    fun setUnchecked() {
        _checked = false
    }

}
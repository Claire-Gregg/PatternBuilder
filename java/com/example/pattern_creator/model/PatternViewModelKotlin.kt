package com.example.pattern_creator.model

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlin.math.pow
import kotlin.math.roundToInt

fun findClosestColour(input: Int, options: List<Colour>): Colour{
    var previousBestIndex = 0
    var previousBestValue =
        255.0.pow(3.0) //max r/g/b value ^ no. of dimensions (want smaller distance)
    for (index in options.indices) {
        if (colourDistance(input, options[index].colour) < previousBestValue) {
            previousBestValue = colourDistance(input, options[index].colour)
            previousBestIndex = index
        }
    }
    return options[previousBestIndex]
}

fun colourDistance(input: Int, compare: Int): Double {
    //formula found from stack overflow https://stackoverflow.com/questions/1725505/finding-similar-colors-programmatically
    return ((Color.red(compare).toDouble() - Color.red(input)).pow(2.0)
            + (Color.green(compare).toDouble() - Color.green(input)).pow(2.0)
            + (Color.blue(compare).toDouble() - Color.blue(input)).pow(2.0)).pow(0.5)
}

class PatternViewModelKotlin : ViewModel() {
    private var _numberOfCheckedColours = 0
    val numberOfCheckedColours: Int
        get() = _numberOfCheckedColours
    private var _photo = MutableLiveData<Bitmap>()
    val photo: LiveData<Bitmap> = _photo
    private var photoWidth = 1.0
    private var photoHeight = 1.0

    private var _coloursChanged = MutableLiveData<Boolean>(false)
    val coloursChanged: LiveData<Boolean> = _coloursChanged

    private var _photoPixelated = MutableLiveData<Bitmap>()
    val photoPixelated: LiveData<Bitmap> = _photoPixelated

    private var _patternWidth = MutableLiveData(1)
    val patternWidth: LiveData<Int> = _patternWidth

    private var _patternPixel = MutableLiveData<Bitmap>()
    val patternPixel: LiveData<Bitmap> = _patternPixel

    private var _patternPhoto = MutableLiveData<Bitmap>()
    val patternPhoto: LiveData<Bitmap> = _patternPhoto

    private val _colourOptions = MutableLiveData(mutableListOf<Colour>())
    val colourOptions: LiveData<MutableList<Colour>> = _colourOptions

    private var _finalColoursUsed = MutableLiveData(mutableSetOf<Colour>())
    val finalColoursUsed: LiveData<MutableSet<Colour>> = _finalColoursUsed


    fun addColourOption(newColour: Colour) {
        Log.d(TAG, "New colour is $newColour")
        _colourOptions.value?.add(newColour)
        _numberOfCheckedColours++
    }

    fun setPatternWidth(widthInput: Int) {
        Log.d(TAG, "newWidth = $widthInput")
        val newWidth: Double = if (widthInput > 0) {
            widthInput.toDouble()
        } else {
            1.0
        }
        _patternWidth.value = newWidth.toInt()
        _patternPixel.value = Bitmap.createScaledBitmap(
            _photo.value!!,
            newWidth.toInt(),
            ((newWidth/photoWidth) * photoHeight).roundToInt(),
        true)
        _photoPixelated.value = Bitmap.createScaledBitmap(
            _patternPixel.value!!,
            photoWidth.roundToInt(),
            photoHeight.roundToInt(),
            true
        )
    }

    fun setPhotoBitmap(inputPhoto: Bitmap) {
        _photo.value = inputPhoto
        _photoPixelated.value = inputPhoto
        _patternWidth.value = inputPhoto.width
        photoWidth = inputPhoto.width.toDouble()
        photoHeight = inputPhoto.height.toDouble()
    }

    fun colourPattern(){
        val input = _patternPixel.value!!
        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        val confirmedColours = findConfirmedColours()

        for (colour in confirmedColours) {
            colour.resetUses()
        }
        for(pixelRow in 0 until output.width) {
            for(pixelCol in 0 until output.height) {
                val newColour = findClosestColour(output.getPixel(pixelRow, pixelCol),
                    confirmedColours)
                newColour.addUse()
                output.setPixel(pixelRow, pixelCol, newColour.colour)
                _finalColoursUsed.value!!.add(newColour)
            }
        }
        _patternPixel.value = output
        _patternPhoto.value = Bitmap.createScaledBitmap(output,
            photoWidth.roundToInt(), photoHeight.roundToInt(), false)
    }
    private fun findConfirmedColours(): List<Colour> {
        val newList = mutableListOf<Colour>()
        for (colour in _colourOptions.value!!){
            if(colour.checked){
                newList.add(colour)
            }
        }
        return newList.toList()
    }

    fun resetChangedColours() {
        _coloursChanged.value = false
    }

    fun storeColours(editor: SharedPreferences.Editor) {
        //TODO: make saved data actually work
        _coloursChanged.value = true
        editor.clear()
        val coloursToSave = (colourOptions.value!!).toList()
        editor.putInt(NUMBER_OF_COLOURS_KEY, coloursToSave.size)
        //Log.d(TAG, "Saving ${coloursToSave.size} colours")
        val gson = Gson()
        for(index in coloursToSave.indices){
            val json = gson.toJson(coloursToSave[index])
            editor.putString("Colour${index}", json)
            //Log.d(TAG, "Saving colour $index")
        }
        editor.apply()
    }

    fun restoreColours(prefs: SharedPreferences?){
        //TODO: make saved data actually work
        if (prefs != null) {
            val gson = Gson()
            val numberOfColours = prefs.getInt(NUMBER_OF_COLOURS_KEY, 0)
            Log.d(TAG, "Restoring $numberOfColours colours")
            _colourOptions.value = mutableListOf()
            for (index in 0 until numberOfColours) {
                val json = prefs.getString("Colour${index}", "")
                val gsonVal = gson.fromJson(json, Colour::class.java)

                if(!_colourOptions.value!!.contains(gsonVal)){
                    _colourOptions.value!!.add(gsonVal)
                    Log.d(TAG, "Restoring colour $index")
                }
            }
        }
    }

    fun checkItem (positionInList: Int) {
        if(_colourOptions.value!!.size > positionInList) {
            _colourOptions.value!![positionInList].check()
            if (_colourOptions.value!![positionInList].checked) {
                _numberOfCheckedColours++
            } else {
                _numberOfCheckedColours--
            }
        }
    }

    companion object {
        const val TAG = "PatternViewModel"
        const val NUMBER_OF_COLOURS_KEY = "numberOfColours"
    }
}


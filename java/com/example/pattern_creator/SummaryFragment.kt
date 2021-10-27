/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.pattern_creator

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.pattern_creator.databinding.FragmentSummaryBinding
import com.example.pattern_creator.model.Colour
import com.example.pattern_creator.model.PatternViewModelKotlin
import com.example.pattern_creator.model.colourDistance
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.collections.set
import kotlin.math.roundToInt

/**
 * [SummaryFragment] contains a summary of the order details with a button to share the order
 * via another app.
 */
class SummaryFragment : Fragment() {

    // Binding object instance corresponding to the fragment_summary.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment.
    private var binding: FragmentSummaryBinding? = null

    private val sharedViewModel: PatternViewModelKotlin by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentSummaryBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        binding!!.finalPatternPreview.setImageBitmap(sharedViewModel.patternPhoto.value)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            summaryFragment = this@SummaryFragment
        }
    }

    private fun getBitmapFromVectorOld(drawableId: Int): Bitmap?{
        var drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        return if (drawable != null) {
            drawable = (DrawableCompat.wrap(drawable)).mutate()
            val bitmap = Bitmap.createBitmap(SYMBOL_SIZE, SYMBOL_SIZE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            null
        }

    }

    fun createPDF() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val numbers = Paint()
        val legend = Paint()
        legend.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        legend.textSize = 50F
        legend.color = Color.BLACK

        val pattern = sharedViewModel.patternPixel.value!!
        val colours = sharedViewModel.finalColoursUsed.value!!.toMutableList()

        val pageInfo = PdfDocument.PageInfo.Builder(getPatternPageWidth(pattern.width, colours, legend),
            getPatternPageHeight(pattern.height, colours, legend), 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val linePoints = mutableListOf<Float>()
        val linePoints10 = mutableListOf<Float>()


        //pattern part
        canvas.translate(PAGE_MARGIN.toFloat(), PAGE_MARGIN.toFloat())


        //assigning colours their symbols
        val colourSymbolMap = mutableMapOf<Int, Bitmap>()
        val colourObjectSymbolMap = mutableMapOf<Bitmap, Colour>()
        var index = 0
        while(index < colours.size){
            @ColorInt
            val colour = colours[index].colour
            val symbolColour: Int =
                if(colourDistance(colour, Color.WHITE) < 100){
                    Color.BLACK
                } else {
                    colour
                }
            Log.d(TAG, "New symbol for ${colours[index].name}")

            var symbol: Bitmap
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                val newSymbol = getBitmapFromVectorOld(chooseSymbol(index+1))
                symbol = if (newSymbol != null) {
                    newSymbol
                } else {
                    val newSymbol = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                    newSymbol.setPixel(0,0, symbolColour)
                    Bitmap.createScaledBitmap(newSymbol, SYMBOL_SIZE, SYMBOL_SIZE, false)
                }
            } else {
                symbol = getBitmapFromVectorDrawable(
                    requireContext(),
                    chooseSymbol(index + 1),
                    SYMBOL_SIZE, SYMBOL_SIZE,
                    symbolColour
                )
            }
            colourSymbolMap[colour] = symbol
            colourObjectSymbolMap[symbol] = colours[index]
            index++
        }

        //drawing pattern in
        for(pixelRow in 0 until pattern.height){
            for(pixelCol in 0 until pattern.width){
                @ColorInt
                val pixelColour: Int = pattern.getPixel(pixelCol, pixelRow)
                val symbol = colourSymbolMap[pixelColour]!!
                canvas.drawBitmap(
                    symbol,
                    Rect(0, 0, symbol.width, symbol.height),
                    Rect(SYMBOL_MARGINS/2, SYMBOL_MARGINS/2,
                        symbol.width + SYMBOL_MARGINS/2, symbol.height + SYMBOL_MARGINS/2),
                    paint)
                canvas.translate(STITCH_SQUARE_SIZE.toFloat(), 0.toFloat())
            }
            canvas.translate((-pattern.width * STITCH_SQUARE_SIZE).toFloat(), STITCH_SQUARE_SIZE.toFloat())
        }
        canvas.translate(0.toFloat(), (-pattern.height * STITCH_SQUARE_SIZE).toFloat())

        numbers.color = Color.BLACK
        numbers.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        numbers.textSize = NUMBER_HEIGHT.toFloat()

        //drawing in lines
        //horizontal lines
        val printedPatternWidth = pattern.width * STITCH_SQUARE_SIZE
        for(pixelRow in 0..pattern.height){
            val y = (pixelRow * STITCH_SQUARE_SIZE)
            linePoints.addAll(0, mutableListOf(
                0.toFloat(), y.toFloat(),
                (printedPatternWidth).toFloat(), y.toFloat()))
            if((pixelRow - 1) % 10 == 9){
                canvas.translate(
                    -(numbers.measureText((pixelRow).toString()) + GAP_BETWEEN_PATTERN_AND_NUMBERS),
                    -((STITCH_SQUARE_SIZE - (numbers.fontMetrics.descent - numbers.fontMetrics.ascent)) / 2)
                )

                canvas.drawText((pixelRow).toString(), 0F, y.toFloat(), numbers)

                canvas.translate(
                    (numbers.measureText((pixelRow).toString()) + GAP_BETWEEN_PATTERN_AND_NUMBERS),
                    ((STITCH_SQUARE_SIZE - (numbers.fontMetrics.descent - numbers.fontMetrics.ascent)) / 2)
                )

                linePoints10.addAll(0, mutableListOf(
                    0.toFloat(), y.toFloat(),
                    (printedPatternWidth).toFloat(), y.toFloat()))
            }
        }

        //vertical lines
        val printedPatternHeight = pattern.height * STITCH_SQUARE_SIZE
        for(pixelCol in 0..pattern.width) {
            val x = (pixelCol * STITCH_SQUARE_SIZE)
            linePoints.addAll(0, mutableListOf(
                x.toFloat(), 0.toFloat(),
                x.toFloat(), (printedPatternHeight).toFloat()))
            if((pixelCol - 1) % 10 == 9){
                canvas.translate(
                    -(numbers.measureText((pixelCol).toString()) / 2),
                    -(GAP_BETWEEN_PATTERN_AND_NUMBERS.toFloat())
                )

                canvas.drawText((pixelCol).toString(), x.toFloat(), 0.toFloat(), numbers)

                canvas.translate(
                    (numbers.measureText((pixelCol).toString()) / 2),
                    (GAP_BETWEEN_PATTERN_AND_NUMBERS.toFloat())
                )
                linePoints10.addAll(0, mutableListOf(
                    x.toFloat(), 0.toFloat(),
                    x.toFloat(), (printedPatternHeight).toFloat()))
            }
        }
        canvas.drawLines(linePoints.toFloatArray(), paint)
        paint.strokeWidth = 3F
        canvas.drawLines(linePoints10.toFloatArray(), paint)
        paint.strokeWidth = 1F
        canvas.translate((MARGIN_BETWEEN_PATTERN_AND_LEGEND + (pattern.width * STITCH_SQUARE_SIZE)).toFloat(), 0.toFloat())


        //legend

        for ((symbol, colour) in colourObjectSymbolMap) {
            canvas.drawBitmap(symbol,
                Rect(0, 0, SYMBOL_SIZE, SYMBOL_SIZE),
                Rect(0, 0, SYMBOL_SIZE, SYMBOL_SIZE),
                legend)
            canvas.translate((SYMBOL_SIZE + MARGIN_BETWEEN_LEGEND_AND_LABELS).toFloat(), SYMBOL_SIZE.toFloat())
            val name = colour.name
            canvas.drawText(name, 0F, 0F, legend)
            canvas.translate(0F, (legend.fontMetricsInt.descent - legend.fontMetricsInt.ascent).toFloat())
            Log.d(TAG, "${colour.numberOfUses}")
            val stitchesNumberText = "${colour.numberOfUses} stitches total"
            canvas.drawText(stitchesNumberText, 0F, 0F, legend)

            canvas.translate(-(SYMBOL_SIZE + MARGIN_BETWEEN_LEGEND_AND_LABELS).toFloat(),
                (15F + MARGIN_BETWEEN_LEGEND_AND_LABELS)
            )
        }

        pdfDocument.finishPage(page)
        val pdfPath = File(requireContext().filesDir, "pdfs")
        pdfPath.mkdir()
        val file = File(pdfPath, "patternExported.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(requireContext(), "PDF File generated", Toast.LENGTH_SHORT).show()
            val contentUri = getUriForFile(requireContext(), "com.example.pattern_creator.fileprovider", file)
            sharePDF(contentUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        pdfDocument.close()

    }

    private fun sharePDF(pdf: Uri) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, pdf)
            type = "application/pdf"
        }
        startActivity(Intent.createChooser(shareIntent, "Send to: "))
    }


    /**
     * This fragment lifecycle method is called when the view hierarchy associated with the fragment
     * is being removed. As a result, clear out the binding object.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    companion object {
        const val TAG = "SummaryFragment"

        //PDF values
        const val PAGE_MARGIN = 400
        const val MARGIN_BETWEEN_PATTERN_AND_LEGEND = 200
        const val MARGIN_BETWEEN_LEGEND_AND_LABELS = 50
        const val GAP_BETWEEN_PATTERN_AND_NUMBERS = 20
        const val NUMBER_HEIGHT = 50

        const val STITCH_SQUARE_SIZE = 50
        const val SYMBOL_MARGINS = 10
        const val SYMBOL_SIZE = STITCH_SQUARE_SIZE - SYMBOL_MARGINS


        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, height: Int, width: Int, colour: Int): Bitmap {
            val drawable = VectorDrawableCompat.create(context.resources, drawableId, null)!!//ContextCompat.getDrawable(context, drawableId)!! as VectorDrawableCompat
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setTint(colour)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }
    }

    private fun getPatternPageWidth (patternWidth: Int, colours: MutableList<Colour>, legendPaint: Paint): Int {
        return PAGE_MARGIN + (patternWidth * STITCH_SQUARE_SIZE) + MARGIN_BETWEEN_PATTERN_AND_LEGEND + getLegendWidth(colours, legendPaint) + PAGE_MARGIN
    }

    private fun getPatternPageHeight (patternHeight: Int, colours: MutableList<Colour>, legendPaint: Paint): Int {
        return if((patternHeight * STITCH_SQUARE_SIZE) > getLegendHeight(colours, legendPaint)) {
            Log.d(TAG, "pattern is larger")
            (patternHeight * STITCH_SQUARE_SIZE) + (2 * PAGE_MARGIN)
        } else {
            getLegendHeight(colours, legendPaint) + (2 * PAGE_MARGIN)
        }
    }

    private fun getLegendWidth (colours: MutableList<Colour>, paint: Paint): Int{
        val legendItemWidths = mutableListOf<Int>()
        for (colour in colours) {
            legendItemWidths.add(SYMBOL_SIZE + MARGIN_BETWEEN_LEGEND_AND_LABELS + paint.measureText(colour.name).roundToInt())
            legendItemWidths.add(SYMBOL_SIZE + MARGIN_BETWEEN_LEGEND_AND_LABELS + paint.measureText(String.format("%d stitches total", colour.numberOfUses)).roundToInt())
        }
        return legendItemWidths.maxOrNull()!!
    }

    private fun getLegendHeight (colours: MutableList<Colour>, paint: Paint): Int {
        return colours.size * ((2 * (paint.fontMetricsInt.descent - paint.fontMetricsInt.ascent)) + MARGIN_BETWEEN_LEGEND_AND_LABELS)
    }

    private fun chooseSymbol(symbolNumber: Int): Int{
        when(symbolNumber){
            1 -> return R.drawable.ic_symbol_1
            2 -> return R.drawable.ic_symbol_2
            3 -> return R.drawable.ic_symbol_3
            4 -> return R.drawable.ic_symbol_4
            5 -> return R.drawable.ic_symbol_5
            6 -> return R.drawable.ic_symbol_6
            7 -> return R.drawable.ic_symbol_7
            8 -> return R.drawable.ic_symbol_8
            9 -> return R.drawable.ic_symbol_9
            10 -> return R.drawable.ic_symbol_10
            11 -> return R.drawable.ic_symbol_11
            12 -> return R.drawable.ic_symbol_12
            13 -> return R.drawable.ic_symbol_13
            14 -> return R.drawable.ic_symbol_14
            15 -> return R.drawable.ic_symbol_15
            16 -> return R.drawable.ic_symbol_16
            17 -> return R.drawable.ic_symbol_17
            18 -> return R.drawable.ic_symbol_18
            19 -> return R.drawable.ic_symbol_19
            20 -> return R.drawable.ic_symbol_20
            21 -> return R.drawable.ic_symbol_21
            22 -> return R.drawable.ic_symbol_22
            23 -> return R.drawable.ic_symbol_23
            24 -> return R.drawable.ic_symbol_24
            25 -> return R.drawable.ic_symbol_25
            26 -> return R.drawable.ic_symbol_26
            27 -> return R.drawable.ic_symbol_27
            28 -> return R.drawable.ic_symbol_28
            29 -> return R.drawable.ic_symbol_29
            30 -> return R.drawable.ic_symbol_30
            31 -> return R.drawable.ic_symbol_31
            32 -> return R.drawable.ic_symbol_32
            33 -> return R.drawable.ic_symbol_33
            34 -> return R.drawable.ic_symbol_34
            35 -> return R.drawable.ic_symbol_35
            36 -> return R.drawable.ic_symbol_36
            37 -> return R.drawable.ic_symbol_37
            38 -> return R.drawable.ic_symbol_38
            39 -> return R.drawable.ic_symbol_39
            40 -> return R.drawable.ic_symbol_40
            41 -> return R.drawable.ic_symbol_41
            42 -> return R.drawable.ic_symbol_42
            43 -> return R.drawable.ic_symbol_43
            44 -> return R.drawable.ic_symbol_44
            45 -> return R.drawable.ic_symbol_45
            46 -> return R.drawable.ic_symbol_46
            47 -> return R.drawable.ic_symbol_47
            48 -> return R.drawable.ic_symbol_48
            49 -> return R.drawable.ic_symbol_49
            50 -> return R.drawable.ic_symbol_50
            51 -> return R.drawable.ic_symbol_51
            52 -> return R.drawable.ic_symbol_52
            53 -> return R.drawable.ic_symbol_53
            54 -> return R.drawable.ic_symbol_54
            55 -> return R.drawable.ic_symbol_55
            56 -> return R.drawable.ic_symbol_56
            57 -> return R.drawable.ic_symbol_57
            58 -> return R.drawable.ic_symbol_58
            59 -> return R.drawable.ic_symbol_59
            60 -> return R.drawable.ic_symbol_60
            61 -> return R.drawable.ic_symbol_61
            62 -> return R.drawable.ic_symbol_62
            63 -> return R.drawable.ic_symbol_63
            64 -> return R.drawable.ic_symbol_64
            65 -> return R.drawable.ic_symbol_65
            66 -> return R.drawable.ic_symbol_66
            67 -> return R.drawable.ic_symbol_67
            68 -> return R.drawable.ic_symbol_68
            69 -> return R.drawable.ic_symbol_69
            70 -> return R.drawable.ic_symbol_70
            71 -> return R.drawable.ic_symbol_71
            72 -> return R.drawable.ic_symbol_72
            73 -> return R.drawable.ic_symbol_73
            74 -> return R.drawable.ic_symbol_74
            75 -> return R.drawable.ic_symbol_75
            76 -> return R.drawable.ic_symbol_76
            77 -> return R.drawable.ic_symbol_77
            78 -> return R.drawable.ic_symbol_78
            79 -> return R.drawable.ic_symbol_79
            80 -> return R.drawable.ic_symbol_80
            81 -> return R.drawable.ic_symbol_81
            82 -> return R.drawable.ic_symbol_82
            83 -> return R.drawable.ic_symbol_83
            84 -> return R.drawable.ic_symbol_84
            85 -> return R.drawable.ic_symbol_85
            86 -> return R.drawable.ic_symbol_86
            87 -> return R.drawable.ic_symbol_87
            88 -> return R.drawable.ic_symbol_88
            89 -> return R.drawable.ic_symbol_89
            90 -> return R.drawable.ic_symbol_90
            91 -> return R.drawable.ic_symbol_91
            92 -> return R.drawable.ic_symbol_92
            93 -> return R.drawable.ic_symbol_93
            94 -> return R.drawable.ic_symbol_94
            95 -> return R.drawable.ic_symbol_95
            96 -> return R.drawable.ic_symbol_96
            97 -> return R.drawable.ic_symbol_97
            98 -> return R.drawable.ic_symbol_98
            99 -> return R.drawable.ic_symbol_99
            100 -> return R.drawable.ic_symbol_100
            101 -> return R.drawable.ic_symbol_101
            102 -> return R.drawable.ic_symbol_102
            103 -> return R.drawable.ic_symbol_103
            104 -> return R.drawable.ic_symbol_104
            105 -> return R.drawable.ic_symbol_105
            106 -> return R.drawable.ic_symbol_106
            107 -> return R.drawable.ic_symbol_107
            else -> {
                return R.drawable.ic_symbol_default
            }
        }
    }
}
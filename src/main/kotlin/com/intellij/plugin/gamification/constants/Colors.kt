package com.intellij.plugin.gamification.constants

import java.awt.Color

class Colors {
    private val mapColors: HashMap<Int, Color> = HashMap()

    private companion object {
        const val totalColorsNum = 150
        const val step = 10
        const val rgbMax = 255
        var red = 255
        var green = 255
        var blue = 0
        var i = 1
    }

    fun setColors() {
        i = 1
        while (red > 0) { // 25 yellow -> green
            mapColors[i] = Color(red, green, blue)
            red -= step
            i++
        }
        red = 0
        while (blue < rgbMax) { // 25 green -> blue
            mapColors[i] = Color(red, green, blue)
            blue += step
            i++
        }
        blue = rgbMax
        while (green > 0) { // 25 blue -> dark blue
            mapColors[i] = Color(red, green, blue)
            green -= step
            i++
        }
        green = 0
        while (red < rgbMax) { // 25 dark blue -> violet
            mapColors[i] = Color(red, green, blue)
            red += step
            i++
        }
        red = rgbMax
        while (blue > 0) { // 25 violet -> red
            mapColors[i] = Color(red, green, blue)
            blue -= step
            i++
        }
        blue = 0
        while (green < rgbMax) { // 25 red -> yellow
            mapColors[i] = Color(red, green, blue)
            green += step
            i++
        }
        green = rgbMax
        i = 1
    }

    fun getColor(level: Int): Color? {
        val clr = mapColors[(level % totalColorsNum + 1)]
        return if (clr != null) {
            clr
        } else {
            setColors()
            mapColors[(level % totalColorsNum + 1)]
        }
    }
}

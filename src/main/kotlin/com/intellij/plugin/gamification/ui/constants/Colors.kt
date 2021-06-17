package com.intellij.plugin.gamification.ui.constants

import java.awt.Color

class Colors {
    private val mapColors: HashMap<Int, Color> = HashMap()

    private companion object {
        const val totalColorsNum = 30
        var i = 1
        const val epsAlpha = 40
        var epsLevels = 5
        var lvl = 0
        const val maxRGB = 255
        const val minRGB = 0
        const val rGreen = 40
        const val bGreen = 99
        const val rVioletOrange = 161
        const val gViolet = 25
        const val gBlueYellow = 215
    }

    fun setColors() {
        i = 1
        lvl = epsLevels
        var alpha = epsAlpha
        while (i <= lvl) { // зеленый
            mapColors[i] = Color(rGreen, maxRGB, bGreen, alpha)
            alpha += epsAlpha
            i++
        }
        lvl += epsLevels
        alpha = epsAlpha
        while (i <= lvl) { // фиолетовый
            mapColors[i] = Color(rVioletOrange, gViolet, maxRGB, alpha)
            alpha += epsAlpha
            i++
        }
        lvl += epsLevels
        alpha = epsAlpha
        while (i <= lvl) { // желтый
            mapColors[i] = Color(maxRGB, gBlueYellow, minRGB, alpha)
            alpha += epsAlpha
            i++
        }
        lvl += epsLevels
        alpha = epsAlpha
        while (i <= lvl) { // синий
            mapColors[i] = Color(minRGB, gBlueYellow, maxRGB, alpha)
            alpha += epsAlpha
            i++
        }
        lvl += epsLevels
        alpha = epsAlpha
        while (i <= lvl) { // красный
            mapColors[i] = Color(maxRGB, minRGB, minRGB, alpha)
            alpha += epsAlpha
            i++
        }
        lvl += epsLevels
        alpha = epsAlpha
        while (i <= lvl) { // оранжевый
            mapColors[i] = Color(maxRGB, rVioletOrange, minRGB, alpha)
            alpha += epsAlpha
            i++
        }
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

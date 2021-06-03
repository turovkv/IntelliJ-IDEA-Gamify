package com.intellij.plugin.gamification.constants

import java.awt.Color
import java.util.Random

class Colors {

    private companion object {
        const val rgbMax = 256
        const val rgbMax1 = 254
        const val rgbMax2 = 255
        const val rand1 = 176
        const val rand2 = 213
    }

    fun getColor(level: Int): Color {
        return when (level) {
            1 -> Color(68, 117, 184)
            2 -> Color(102, 173, 220)
            3 -> Color(130, 106, 237)
            4 -> Color(33, 42, 73)
            5 -> Color(200, 121, 255)
            6 -> Color(59, 244, 251)
            7 -> Color(244, 183, 255)
            8 -> Color(202, 255, 138)
            9 -> Color(67, 124, 144)
            10 -> Color(129, 133, 142)
            11 -> Color(169, 135, 67)
            12 -> Color(247, 197, 72)
            13 -> Color(37, 89, 87)
            14 -> Color(67, 124, 144)
            15 -> Color(240, 108, 155)
            16 -> Color(245, 212, 145)
            17 -> Color(97, 160, 175)
            18 -> Color(195, 201, 233)
            19 -> Color(139, 137, 130)
            20 -> Color(55, 63, 71)
            21 -> Color(22, 186, 197)
            22 -> Color(88, 99, 248)
            23 -> Color(58, 95, 119)
            24 -> Color(1, 22, 56)
            25 -> Color(54, 65, 86)
            26 -> Color(33, 78, 52)
            27 -> Color(223, 248, 235)
            28 -> Color(213, 87, 59)
            29 -> Color(136, 80, 83)
            30 -> Color(119, 125, 167)
            31 -> Color(148, 201, 169)
            32 -> Color(198, 236, 174)
            33 -> return Color(198, 236, 174)
            34 -> return Color(109, 26, 54)
            35 -> return Color(99, 83, 91)
            36 -> return Color(252, 208, 161)
            37 -> return Color(177, 182, 149)
            38 -> return Color(83, 145, 126)
            39 -> return Color(247, 162, 120)
            40 -> return Color(200, 233, 160)
            41 -> return Color(109, 211, 206)
            42 -> return Color(161, 61, 99)
            43 -> return Color(53, 30, 41)
            44 -> return Color(139, 232, 203)
            45 -> return Color(136, 141, 167)
            46 -> return Color(156, 122, 151)
            47 -> return Color(48, 54, 51)
            48 -> return Color(202, 60, 37)
            49 -> return Color(230, 170, 104)
            50 -> return Color(255, 251, 189)
            51 -> return Color(127, 176, 105)
            52 -> return Color(222, 143, 110)
            53 -> return Color(125, 124, 132)
            54 -> return Color(45, 147, 173)
            55 -> return Color(136, 171, 117)
            56 -> return Color(219, 213, 110)
            else -> {
                val rnd = Random()
                val temp = level.toLong()
                rnd.setSeed(temp)
                val a = rnd.nextInt() % rgbMax
                val b = (a - temp * rand1) % rgbMax2
                val c = (b + temp * rand2) % rgbMax1
                return Color(a, b.toInt(), c.toInt())
            }
        }
    }
}
package com.intellij.plugin.gamification.ui

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.geom.Arc2D
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicProgressBarUI

class ProgressCircleUI : BasicProgressBarUI() {

    private companion object {
        const val round = 360
        const val numb = .5
        const val angle = 90
        const val bgRGB = 0xDDDDDD
    }

    override fun getPreferredSize(c: JComponent): Dimension {
        val d = super.getPreferredSize(c)
        val v = d.width.coerceAtLeast(d.height)
        d.setSize(v, v)
        return d
    }

    override fun paint(g: Graphics, c: JComponent) {
        val b = progressBar.insets
        val barRectWidth = progressBar.width - b.right - b.left
        val barRectHeight = progressBar.height - b.top - b.bottom
        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return
        }
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val degree = round * progressBar.percentComplete
        val sz = barRectWidth.coerceAtMost(barRectHeight).toDouble()
        val cx = b.left + barRectWidth * numb
        val cy = b.top + barRectHeight * numb
        val or = sz * numb

        val ir = or * numb
        val inner: Shape = Ellipse2D.Double(cx - ir, cy - ir, ir * 2, ir * 2)
        val outer: Shape = Ellipse2D.Double(cx - or, cy - or, sz, sz)
        val sector: Shape = Arc2D.Double(cx - or, cy - or, sz, sz, angle - degree, degree, Arc2D.PIE)
        val foreground = Area(sector)
        val background = Area(outer)
        val hole = Area(inner)
        foreground.subtract(hole)
        background.subtract(hole)
        g2.paint = Color(bgRGB)
        g2.fill(background)
        g2.paint = progressBar.foreground
        g2.fill(foreground)
        g2.dispose()
        if (progressBar.isStringPainted) {
            paintString(g, b.left, b.top, barRectWidth, barRectHeight, 0, b)
        }
    }
}

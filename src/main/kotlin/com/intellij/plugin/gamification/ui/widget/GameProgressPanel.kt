package com.intellij.plugin.gamification.ui.widget

import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl
import com.intellij.openapi.wm.impl.status.TextPanel
import com.intellij.plugin.gamification.ui.constants.Colors
import com.intellij.plugin.gamification.listeners.GameEvent
import com.intellij.plugin.gamification.listeners.GameEventListener
import com.intellij.plugin.gamification.mechanics.GameMechanicsImpl
import com.intellij.plugin.gamification.services.RewardStatisticsService
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Color
import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.JComponent

class GameProgressPanel : TextPanel(), CustomStatusBarWidget {
    private var myStatusBar: StatusBar? = null
    private var progress: Int = 0
    private var level: Int = 0
    private var myColors = Colors()

    companion object {
        const val WIDGET_ID = "Gamify"
        private var color = Color(164, 71, 255, 200)
    }

    override fun getBackground(): Color? {
        return null
    }

    override fun install(statusBar: StatusBar) {
        if (statusBar is IdeStatusBarImpl) {
            statusBar.border = BorderFactory.createEmptyBorder(1, 0, 0, 1)
        }
    }

    override fun dispose() {
        if (myStatusBar is IdeStatusBarImpl) {
            (myStatusBar as IdeStatusBarImpl).border = BorderFactory.createEmptyBorder(1, 0, 0, 0)
        }
        myStatusBar = null
    }

    override fun getPresentation(): WidgetPresentation? {
        return null
    }

    override fun ID(): String {
        return WIDGET_ID
    }

    override fun getComponent(): JComponent {
        return this
    }

    public override fun paintComponent(g: Graphics) {
        val barWidth = size.width
        val progressBarLength = barWidth * progress / GameMechanicsImpl.maxProgress

        g.color = UIUtil.getPanelBackground()
        g.fillRect(0, 0, barWidth, size.height - 1)

        g.color = color
        g.fillRect(0, 0, progressBarLength, size.height - 1)

        text = "Level: $level"

        super.paintComponent(g)
    }

    fun updateState(event: GameEvent) {
        progress = event.progress
        level = event.level

        myColors.setColors()
        color = myColors.getColor(level)!!

        if (!isShowing) {
            return
        }
        repaint()
    }

    init {
        isFocusable = false
        setTextAlignment(CENTER_ALIGNMENT)
        border = JBUI.Borders.empty(0, 2)
        updateUI()

        updateState(
            RewardStatisticsService.getInstance().getCurrentGameEvent()
        )

        RewardStatisticsService
            .getInstance()
            .addListener(
                object : GameEventListener {
                    override fun progressChanged(event: GameEvent) {
                        updateState(event)
                    }
                },
                this
            )
    }
}

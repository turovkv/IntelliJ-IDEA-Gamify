package com.intellij.plugin.gamification.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.plugin.gamification.listeners.GameEvent
import com.intellij.plugin.gamification.listeners.GameEventListener
import com.intellij.plugin.gamification.services.RewardInfoItem
import com.intellij.plugin.gamification.services.RewardStatisticsService
import com.intellij.plugin.gamification.ui.ProgressCircleUI
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.ListTableModel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionListener
import java.util.Random
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import kotlin.Comparator

class GameStatisticsDialog(project: Project?, listener: ActionListener?) : DialogWrapper(project, true) {

    val splitter = Splitter(true)
    val list = listener

    private companion object {
        const val rgbMax = 256
        const val rgbMax1 = 254
        const val rgbMax2 = 255
        const val rand1 = 176
        const val rand2 = 213
        const val textSize = 24

        object Dialog {
            const val with = 400
            const val height = 300
        }

        private val DISPLAY_NAME: ColumnInfo<RewardInfoItem, String> = object :
            ColumnInfo<RewardInfoItem, String>("Feature") {
            override fun valueOf(item: RewardInfoItem?): String {
                return item?.featureName.toString()
            }

            override fun getComparator(): Comparator<RewardInfoItem>? {
                return Comparator.comparing(RewardInfoItem::featureName)
            }
        }

        private val POINTS: ColumnInfo<RewardInfoItem, String> = object :
            ColumnInfo<RewardInfoItem, String>("Points") {
            override fun valueOf(item: RewardInfoItem?): String {
                return item?.points.toString()
            }

            override fun getComparator(): Comparator<RewardInfoItem>? {
                return Comparator.comparing(RewardInfoItem::points)
            }
        }

        private val COLUMNS = arrayOf<ColumnInfo<*, *>>(DISPLAY_NAME, POINTS)
    }

    init {
        title = "Gamify"
        isModal = false
        init()
    }

    override fun getInitialSize(): Dimension {
        return JBDimension(Dialog.with, Dialog.height)
    }

    override fun getDimensionServiceKey(): String {
        return "#com.intellij.plugin.gamification.actions.GameStatisticsDialog"
    }

    override fun createCenterPanel(): JPanel {
        val stats = RewardStatisticsService.getInstance()
        val rewards = stats.getRewardInfo()
        val table = TableView(ListTableModel(COLUMNS, rewards, 0))
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val tablePanel = JPanel(BorderLayout())
        tablePanel.add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
        ScrollingUtil.ensureSelectionExists(table)

        val contentPanel = JPanel()
        val layout = GridBagLayout()
        val gbc = GridBagConstraints()
        contentPanel.layout = layout

        val progress = JProgressBar()
        progress.isStringPainted = true
        progress.value = stats.getProgress()

        val statsInfo = JLabel("Level: " + stats.getLevel().toString(), SwingConstants.CENTER)
        statsInfo.font = Font("Calibri", Font.PLAIN, textSize)
        val clearButton = JButton("Clear Stats")
        val logOutButton = JButton("Log Out")

        val progress1: JProgressBar = object : JProgressBar() {
            override fun updateUI() {
                super.updateUI()
                setUI(ProgressCircleUI())
            }
        }
        var level = stats.getLevel()

        val rnd = Random()
        var temp = level.toLong()
        rnd.setSeed(temp)
        var a = rnd.nextInt() % rgbMax
        var b = (a - temp * rand1) % rgbMax2
        var c = (b + temp * rand2) % rgbMax1

        progress1.foreground = Color(kotlin.math.abs(a), kotlin.math.abs(b).toInt(), kotlin.math.abs(c).toInt())
        val model = progress.model
        progress1.model = model

        fun update() {
            level = stats.getLevel()

            temp = level.toLong()
            rnd.setSeed(temp)
            a = rnd.nextInt() % rgbMax
            b = (a - temp * rand1) % rgbMax2
            c = (b + temp * rand2) % rgbMax1
            progress1.foreground =
                Color(kotlin.math.abs(a), kotlin.math.abs(b).toInt(), kotlin.math.abs(c).toInt())
        }

        clearButton.addActionListener {
            stats.clear()
            update()
        }

        logOutButton.addActionListener(list)

        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.gridx = 0
        gbc.gridy = 0
        contentPanel.add(progress1, gbc)
        gbc.gridx = 0
        gbc.gridy = 1
        contentPanel.add(statsInfo, gbc)
        tablePanel.add(clearButton, BorderLayout.SOUTH)
        tablePanel.add(logOutButton, BorderLayout.NORTH)

        splitter.isShowDividerControls = true
        splitter.firstComponent = contentPanel
        splitter.secondComponent = tablePanel

        stats.addListener(
            object : GameEventListener {
                override fun progressChanged(event: GameEvent) {
                    progress.value = event.progress
                    statsInfo.text = "Level: ${event.level}"
                    if (stats.getLevel() != level && statsInfo.text != "Level: 0") {
                        update()
                    }
                    table.setModelAndUpdateColumns(
                        ListTableModel(COLUMNS, stats.getRewardInfo(), 0)
                    )
                    repaint()
                }
            },
            disposable
        )

        return splitter
    }
}

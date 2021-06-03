package com.intellij.plugin.gamification.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.plugin.gamification.constants.Colors
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
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JProgressBar
import javax.swing.JToolBar
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import kotlin.Comparator

class GameStatisticsDialog(project: Project?, listener: ActionListener?) : DialogWrapper(project, true) {

    val splitter = Splitter(true)
    private val list = listener
    val stats = RewardStatisticsService.getInstance()
    private val color = Colors()
    val toolBar = JToolBar()
    val popupMenu = JPopupMenu()
    var popupShown = false

    private companion object {
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

    fun createBox() {
        val box = Box.createHorizontalBox()
        val stnButton = JButton("Settings")

        box.add(stnButton)

        stnButton.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                val shown = popupShown
                SwingUtilities.invokeLater { popupShown = shown }
            }
        })

        stnButton.addActionListener {
            if (popupShown) {
                popupMenu.isVisible = false
                popupShown = false
            } else {
                popupMenu.show(stnButton, 0, stnButton.height)
            }
        }
        toolBar.add(box)
    }

    override fun createCenterPanel(): JPanel {
        val rewards = stats.getRewardInfo()
        val table = TableView(ListTableModel(COLUMNS, rewards, 0))
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val tablePanel = JPanel(BorderLayout())
        tablePanel.add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
        ScrollingUtil.ensureSelectionExists(table)

        createBox()

        val contentPanel = JPanel()
        val layout = GridBagLayout()
        val gbc = GridBagConstraints()
        contentPanel.layout = layout

        val progress = JProgressBar()
        progress.isStringPainted = true
        progress.value = stats.getProgress()

        val statsInfo = JLabel("Level: " + stats.getLevel().toString(), SwingConstants.CENTER)
        statsInfo.font = Font("Calibri", Font.PLAIN, textSize)
        val clearButton = JMenuItem("Clear Stats")
        val logOutButton = JMenuItem("Log Out")

        val progress1: JProgressBar = object : JProgressBar() {
            override fun updateUI() {
                super.updateUI()
                setUI(ProgressCircleUI())
            }
        }

        progress1.foreground = color.getColor(stats.getLevel())
        val model = progress.model
        progress1.model = model

        clearButton.addActionListener {
            stats.clear()
            progress1.foreground = color.getColor(stats.getLevel())
        }

        logOutButton.addActionListener(list)

        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.gridx = 0
        gbc.gridy = 0
        contentPanel.add(progress1, gbc)
        gbc.gridx = 0
        gbc.gridy = 1
        contentPanel.add(statsInfo, gbc)
        popupMenu.add(clearButton)
        popupMenu.add(logOutButton)
        tablePanel.add(toolBar, BorderLayout.SOUTH)

        splitter.isShowDividerControls = true
        splitter.firstComponent = contentPanel
        splitter.secondComponent = tablePanel

        stats.addListener(
            object : GameEventListener {
                override fun progressChanged(event: GameEvent) {
                    progress.value = event.progress
                    statsInfo.text = "Level: ${event.level}"
                    progress1.foreground = color.getColor(stats.getLevel())
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

package com.intellij.plugin.gamification.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.plugin.gamification.config.UI
import com.intellij.plugin.gamification.services.RewardInfoItem
import com.intellij.plugin.gamification.services.RewardStatisticsService
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.ListTableModel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.ListSelectionModel

class ShowGameStatisticsDialog(project: Project?) : DialogWrapper(project, true) {
    companion object {
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
        return JBDimension(UI.Dialog.with, UI.Dialog.height)
    }

    private fun stats() = RewardStatisticsService.getInstance()

    override fun createCenterPanel(): JPanel {
        val rewards = stats().getRewardInfo()
        val table = TableView(ListTableModel(COLUMNS, rewards, 0))
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val tablePanel = JPanel(BorderLayout())
        tablePanel.add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
        ScrollingUtil.ensureSelectionExists(table)

        val contentPanel = JPanel(BorderLayout())

        val progress = JProgressBar()
        progress.isStringPainted = true
        progress.value = stats().getProgress()

        val levelInfo = JLabel("Level: " + stats().getLevel())
        val clearButton = JButton("Clear Stats")
        clearButton.addActionListener {
            stats().clear()
            progress.value = stats().getProgress()
            levelInfo.text = "Level: ${stats().getLevel()}"
            table.setModelAndUpdateColumns(
                ListTableModel(COLUMNS, stats().getRewardInfo(), 0)
            )
        }

        contentPanel.add(JLabel("Your progress: "), BorderLayout.NORTH)
        contentPanel.add(progress, BorderLayout.CENTER)
        contentPanel.add(clearButton, BorderLayout.SOUTH)
        contentPanel.add(levelInfo, BorderLayout.EAST)

        val splitter = Splitter(true)
        splitter.isShowDividerControls = true
        splitter.firstComponent = contentPanel
        splitter.secondComponent = tablePanel

        return splitter
    }
}

package com.intellij.plugin.gamification.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.plugin.gamification.RewardLogItem
import com.intellij.plugin.gamification.services.SavedStatistics
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.TableViewSpeedSearch
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.ListTableModel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*
import javax.swing.border.EmptyBorder


class FeatureGamificationButton : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        EventQueue.invokeLater {
            ShowGameStatisticsDialog(getEventProject(event)).show()
        }
    }
}

class ShowGameStatisticsDialog(project: Project?) : DialogWrapper(project, true) {

    companion object {
        private val DISPLAY_NAME: ColumnInfo<RewardLogItem, String> = object :
            ColumnInfo<RewardLogItem, String>("DISPLAY_NAME") {
            override fun valueOf(item: RewardLogItem?): String {
                return item?.featureName.toString()
            }

            override fun getComparator(): Comparator<RewardLogItem>? {
                return Comparator.comparing(RewardLogItem::featureName)
            }
        }

        private val POINTS: ColumnInfo<RewardLogItem, String> = object :
            ColumnInfo<RewardLogItem, String>("POINTS") {
            override fun valueOf(item: RewardLogItem?): String {
                return item?.points.toString()
            }

            override fun getComparator(): Comparator<RewardLogItem>? {
                return Comparator.comparing(RewardLogItem::points)
            }
        }

        private val COLUMNS = arrayOf<ColumnInfo<*, *>>(DISPLAY_NAME, POINTS)
    }

    init {
        title = "MyTitle"
        isModal = false
        init()
    }

    override fun getInitialSize(): Dimension {
        return JBDimension(400, 300)
    }

    override fun createCenterPanel(): JComponent {
        val contentPane: JPanel


        contentPane = JPanel()
        contentPane.border = EmptyBorder(5, 5, 5, 5)
        contentPane.layout = BorderLayout(0, 0)

        val progress = JProgressBar()

        val levelInfo = JLabel("Level: " + SavedStatistics.get().level.toString())

        val clearButton = JButton("Clear Stats")
        clearButton.addActionListener {
            SavedStatistics.get().clear()
            progress.value = SavedStatistics.get().getProgress()
            levelInfo.setText("Level: " + SavedStatistics.get().level.toString())
        }

        progress.isStringPainted = true


        contentPane.add(JLabel("Your progress: "), BorderLayout.NORTH)
        contentPane.add(progress, BorderLayout.CENTER)
        contentPane.add(clearButton, BorderLayout.SOUTH)
        contentPane.add(levelInfo, BorderLayout.EAST)



        val worker = ProgressWorker(progress)
        worker.execute()


        val splitter = Splitter(true)
        splitter.isShowDividerControls = true
        val rewards = SavedStatistics.get().getRewardLog()

        val table = TableView(ListTableModel(COLUMNS, rewards, 0))
        object : TableViewSpeedSearch<RewardLogItem>(table) {
            override fun getItemText(element: RewardLogItem): String {
                return element.featureName
            }
        }

        val topPanel = JPanel(BorderLayout())
        topPanel.add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
        ScrollingUtil.ensureSelectionExists(table)

        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        splitter.firstComponent = topPanel

        splitter.secondComponent = contentPane



        return splitter
    }
}


private class ProgressWorker(private val progress: JProgressBar) : SwingWorker<Void?, Int?>() {
    @Throws(Exception::class)
    override fun doInBackground(): Void? {
        return null
    }

    override fun process(chunks: List<Int?>) {
        progress.value = chunks[chunks.size - 1]!!
        super.process(chunks)
    }

    override fun done() {
        progress.value = SavedStatistics.get().getProgress()
    }
}
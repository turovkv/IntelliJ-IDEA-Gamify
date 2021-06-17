package com.intellij.plugin.gamification.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.plugin.gamification.constants.Colors
import com.intellij.plugin.gamification.listeners.GameEvent
import com.intellij.plugin.gamification.listeners.GameEventListener
import com.intellij.plugin.gamification.services.RewardInfoItem
import com.intellij.plugin.gamification.services.RewardStatisticsService
import com.intellij.plugin.gamification.toolWindow.AchievementsPanel
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
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.Box
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JProgressBar
import javax.swing.JTextArea
import javax.swing.JToolBar
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import kotlin.Comparator

class GameStatisticsDialog(project: Project?, fstListner: ActionListener?) :
    DialogWrapper(project, true) {

    val stats = RewardStatisticsService.getInstance()
    val achPanel = AchievementsPanel(stats)

    val logStatus = false
    val splitter = Splitter(true)
    private val list2 = fstListner
    private val color = Colors()
    private val toolBar = JToolBar()
    private val popupMenu = JPopupMenu()
    var popupShown = false
    var popupShownTable = false
    var popupShownInfo = false
    private val rewards = stats.getRewardInfo()
    val table = TableView(ListTableModel(COLUMNS, rewards, 0))

    private companion object {
        const val textSize = 24
        const val buttonSize = 37
        const val frameWidth = 750
        const val frameHeight = 350
        const val tableWidth = 400
        const val tableHeight = 300

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

    private fun createSettings(): JButton {
        val stnButton = JButton(ImageIcon(GameStatisticsDialog::class.java.getResource("/gear30.png")))

        stnButton.preferredSize = Dimension(buttonSize, buttonSize)

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
        stnButton.isBorderPainted = false
        stnButton.isOpaque = false
        stnButton.isContentAreaFilled = false
        return stnButton
    }

    private fun createInfo(): JButton {
        val questionButton = JButton(ImageIcon(GameStatisticsDialog::class.java.getResource("/info30.png")))

        val textArea = JTextArea()

        val fileName = GameStatisticsDialog::class.java.getResource("/GameLogic.txt")
        val text = BufferedReader(InputStreamReader(fileName.openStream()))
        var line: String?
        while (text.readLine().also { line = it } != null) {
            textArea.append(line)
            textArea.append("\n")
        }
        text.close()

        textArea.isEditable = false

        val popup = JPopupMenu()
        val popPanel = JPanel(BorderLayout())
        popPanel.add(ScrollPaneFactory.createScrollPane(textArea), BorderLayout.CENTER)
        popup.add(popPanel)

        popup.maximumSize = Dimension(frameWidth, frameHeight)
        popup.preferredSize = Dimension(frameWidth, frameHeight)
        popup.minimumSize = Dimension(frameWidth, frameHeight)

        questionButton.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                val shown = popupShownInfo
                SwingUtilities.invokeLater { popupShownInfo = shown }
            }
        })

        questionButton.addActionListener {
            if (popupShownInfo) {
                popup.isVisible = false
                popupShownInfo = false
            } else {
                popup.show(questionButton, 0, questionButton.height)
            }
        }

        questionButton.preferredSize = Dimension(buttonSize, buttonSize)

        questionButton.isBorderPainted = false
        questionButton.isOpaque = false
        questionButton.isContentAreaFilled = false
        return questionButton
    }

    private fun createTable(): JButton {
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val tablePanel = JPanel(BorderLayout())
        tablePanel.add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
        ScrollingUtil.ensureSelectionExists(table)

        val popup = JPopupMenu()
        popup.add(tablePanel)

        popup.maximumSize = Dimension(tableWidth, tableHeight)
        popup.preferredSize = Dimension(tableWidth, tableHeight)
        popup.minimumSize = Dimension(tableWidth, tableHeight)

        val tableButton = JButton(ImageIcon(GameStatisticsDialog::class.java.getResource("/activity30.png")))
        tableButton.preferredSize = Dimension(buttonSize, buttonSize)

        tableButton.isBorderPainted = false
        tableButton.isOpaque = false
        tableButton.isContentAreaFilled = false

        tableButton.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                val shown = popupShownTable
                SwingUtilities.invokeLater { popupShownTable = shown }
            }
        })

        tableButton.addActionListener {
            if (popupShownTable) {
                popup.isVisible = false
                popupShownTable = false
            } else {
                popup.show(tableButton, 0, tableButton.height)
            }
        }
        return tableButton
    }

    private fun createBox() {
        toolBar.add(Box.createHorizontalGlue())
        toolBar.add(createSettings())
        toolBar.add(createInfo())
        toolBar.add(createTable())
        toolBar.isFloatable = false
    }

    override fun createCenterPanel(): JPanel {

        createBox()

        val progress = JProgressBar()
        progress.isStringPainted = true
        progress.value = stats.getProgress()

        val statsInfo = JLabel("Level: " + stats.getLevel().toString(), SwingConstants.CENTER)
        statsInfo.font = Font("Calibri", Font.PLAIN, textSize)
        val clearButton = JMenuItem("Clear Stats")
        val logOutButton = JMenuItem("Log Out")
        val logInButton = JMenuItem("Log In")

        val progress1: JProgressBar = object : JProgressBar() {
            override fun updateUI() {
                super.updateUI()
                setUI(ProgressCircleUI())
            }
        }

        color.setColors()
        var setClr = color.getColor(stats.getLevel())
        if (setClr == null) {
            color.setColors()
            setClr = color.getColor(stats.getLevel())
        }
        progress1.foreground = setClr
        val model = progress.model
        progress1.model = model

        clearButton.addActionListener {
            stats.clear()
            achPanel.clearAchievements()
            setClr = color.getColor(stats.getLevel())
            progress1.foreground = setClr
        }

        logOutButton.addActionListener(list2)
        logInButton.addActionListener(list2)

        val contentPanel = JPanel()
        val layout = GridBagLayout()
        val gbc = GridBagConstraints()
        contentPanel.layout = layout

        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.gridx = 0
        gbc.gridy = 0
        contentPanel.add(progress1, gbc)
        gbc.gridx = 0
        gbc.gridy = 1
        contentPanel.add(statsInfo, gbc)
        popupMenu.add(clearButton)

        if (!logStatus) popupMenu.add(logInButton)
        else popupMenu.add(logOutButton)

        val panel = JPanel(BorderLayout())
        panel.add(toolBar, BorderLayout.PAGE_START)
        panel.add(contentPanel, BorderLayout.CENTER)

        splitter.isShowDividerControls = true
        splitter.firstComponent = panel
        splitter.secondComponent = achPanel.createAchievementsPanel()

        stats.addListener(
            object : GameEventListener {
                override fun progressChanged(event: GameEvent) {
                    progress.value = event.progress
                    statsInfo.text = "Level: ${event.level}"
                    setClr = color.getColor(stats.getLevel())
                    progress1.foreground = setClr
                    table.setModelAndUpdateColumns(
                        ListTableModel(COLUMNS, stats.getRewardInfo(), 0)
                    )
                    achPanel.setAchievementsState()
                    repaint()
                }
            },
            disposable
        )
        return splitter
    }
}

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
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.ArrayList
import javax.imageio.ImageIO
import javax.swing.Box
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
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

class GameStatisticsDialog(project: Project?, listener: ActionListener?) : DialogWrapper(project, true) {

    val splitter = Splitter(true)
    private val list = listener
    val stats = RewardStatisticsService.getInstance()
    private val color = Colors()
    private val toolBar = JToolBar()
    private val popupMenu = JPopupMenu()
    var popupShown = false
    var popupShown1 = false
    private val rewards = stats.getRewardInfo()
    val table = TableView(ListTableModel(COLUMNS, rewards, 0))
    private val achievementsPanel = JPanel()
    private val layoutAchievements = GridBagLayout()
    private val gbcAchievements = GridBagConstraints()

    private var achievementsMap: ArrayList<Achievements> = ArrayList(numOfAchievements)
    private var iconsMap: ArrayList<JLabel> = ArrayList(numOfAchievements)

    private var wPic: BufferedImage = ImageIO.read(this.javaClass.getResource("/empty.png"))

    private companion object {
        const val textSize = 24
        const val buttonSize = 37
        const val frameWidth = 750
        const val frameHeight = 350
        const val tableWidth = 400
        const val tableHeight = 300
        const val numOfAchievements = 6
        const val achievement1 = 5
        const val achievement2 = 10
        const val achievement3 = 15
        const val achievement4 = 20
        const val achievement5 = 25
        const val achievement6 = 50
        const val achievementTextSize = 14
        const val dist = 10
        const val row = 3

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

        questionButton.addActionListener {
            val f = JFrame()
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
            f.add(textArea)
            f.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

            f.setSize(frameWidth, frameHeight)
            f.isVisible = true
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
                val shown = popupShown1
                SwingUtilities.invokeLater { popupShown1 = shown }
            }
        })

        tableButton.addActionListener {
            if (popupShown1) {
                popup.isVisible = false
                popupShown1 = false
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
    }

    private fun addAchievements() {
        achievementsMap.add(
            Achievements(
                "/green.png",
                "<html><body style='text-align: center'>5<br/>new Features</html>",
                achievement1
            )
        )
        achievementsMap.add(
            Achievements(
                "/blue.png",
                "<html><body style='text-align: center'>10<br/>new Features</html>",
                achievement2
            )
        )
        achievementsMap.add(
            Achievements(
                "/violet.png",
                "<html><body style='text-align: center'>15<br/>new Features</html>",
                achievement3
            )
        )
        achievementsMap.add(
            Achievements(
                "/yellow.png",
                "<html><body style='text-align: center'>20<br/>new Features</html>",
                achievement4
            )
        )
        achievementsMap.add(
            Achievements(
                "/orange.png",
                "<html><body style='text-align: center'>25<br/>new Features</html>",
                achievement5
            )
        )
        achievementsMap.add(
            Achievements(
                "/red.png",
                "<html><body style='text-align: center'>50<br/>new Features</html>",
                achievement6
            )
        )
    }

    private fun createAchievementsPanel(): JPanel {
        achievementsPanel.layout = layoutAchievements

        addAchievements()
        var x = 0
        var y: Int
        var coordCounter = 0
        var diff = 0
        gbcAchievements.fill = GridBagConstraints.HORIZONTAL
        for ((iter, elem) in achievementsMap.withIndex()) {
            val nameLabel = JLabel(elem.name, SwingConstants.CENTER)
            nameLabel.font = Font("Calibri", Font.PLAIN, achievementTextSize)

            iconsMap.add(JLabel())
            iconsMap[iter].icon = ImageIcon(wPic)
            y = diff
            gbcAchievements.gridx = x
            gbcAchievements.gridy = y
            achievementsPanel.add(iconsMap[iter], gbcAchievements)
            y = (diff + 1)
            gbcAchievements.gridx = x
            gbcAchievements.gridy = y
            achievementsPanel.add(nameLabel, gbcAchievements)
            coordCounter++
            x += dist
            if (coordCounter == row) {
                x = 0
                diff += dist
                coordCounter = 0
            }
        }

        setAchievementsState()

        return achievementsPanel
    }

    private fun setAchievementsState() {
        for ((iter, elem) in achievementsMap.withIndex()) {
            if (stats.getRewardInfo().size >= elem.number) {
                iconsMap[iter].icon = elem.icon
            }
        }
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
            wPic = ImageIO.read(this.javaClass.getResource("/empty.png"))
            for ((iter, _) in achievementsMap.withIndex()) {
                iconsMap[iter].icon = ImageIcon(wPic)
            }
            setClr = color.getColor(stats.getLevel())
            progress1.foreground = setClr
        }

        logOutButton.addActionListener(list)

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
        popupMenu.add(logOutButton)

        val panel = JPanel(BorderLayout())
        panel.add(toolBar, BorderLayout.PAGE_START)
        panel.add(contentPanel, BorderLayout.CENTER)

        createAchievementsPanel()

        splitter.isShowDividerControls = true
        splitter.firstComponent = panel
        splitter.secondComponent = achievementsPanel

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
                    setAchievementsState()
                    repaint()
                }
            },
            disposable
        )
        return splitter
    }

    private class Achievements(newIcon: String, newName: String, stats: Int) {
        private val wPic: BufferedImage = ImageIO.read(this.javaClass.getResource(newIcon))
        var icon = ImageIcon(wPic)
        val name = newName
        val number = stats
    }
}

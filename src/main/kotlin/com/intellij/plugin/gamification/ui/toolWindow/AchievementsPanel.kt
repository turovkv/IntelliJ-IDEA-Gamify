package com.intellij.plugin.gamification.ui.toolWindow

import com.intellij.plugin.gamification.services.RewardStatisticsService
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.image.BufferedImage
import java.util.ArrayList
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class AchievementsPanel(stats: RewardStatisticsService) {
    private val stats1 = stats
    private val panel = JPanel()
    private val layoutAchievements = GridBagLayout()
    private val gbcAchievements = GridBagConstraints()
    private var achievementsMap: ArrayList<Achievements> = ArrayList(numOfAchievements)
    private var iconsMap: ArrayList<JLabel> = ArrayList(numOfAchievements)
    private var wPic: BufferedImage = ImageIO.read(this.javaClass.getResource("/icons/empty.png"))

    private companion object {
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
    }

    private fun addAchievements() {
        achievementsMap.add(
            Achievements(
                "/icons/green.png",
                "<html><body style='text-align: center'>5<br/>new Features</html>",
                achievement1
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/blue.png",
                "<html><body style='text-align: center'>10<br/>new Features</html>",
                achievement2
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/violet.png",
                "<html><body style='text-align: center'>15<br/>new Features</html>",
                achievement3
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/yellow.png",
                "<html><body style='text-align: center'>20<br/>new Features</html>",
                achievement4
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/orange.png",
                "<html><body style='text-align: center'>25<br/>new Features</html>",
                achievement5
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/red.png",
                "<html><body style='text-align: center'>50<br/>new Features</html>",
                achievement6
            )
        )
    }

    fun setAchievementsState() {
        for ((iter, elem) in achievementsMap.withIndex()) {
            if (stats1.getRewardInfo().size >= elem.number) {
                iconsMap[iter].icon = elem.icon
            }
        }
    }

    fun createAchievementsPanel(): JPanel {
        panel.layout = layoutAchievements

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
            panel.add(iconsMap[iter], gbcAchievements)
            y = (diff + 1)
            gbcAchievements.gridx = x
            gbcAchievements.gridy = y
            panel.add(nameLabel, gbcAchievements)
            coordCounter++
            x += dist
            if (coordCounter == row) {
                x = 0
                diff += dist
                coordCounter = 0
            }
        }

        setAchievementsState()

        return panel
    }

    fun clearAchievements() {
        wPic = ImageIO.read(this.javaClass.getResource("/icons/empty.png"))
        for ((iter, _) in achievementsMap.withIndex()) {
            iconsMap[iter].icon = ImageIcon(wPic)
        }
    }

    private class Achievements(newIcon: String, newName: String, stats: Int) {
        private val wPic: BufferedImage = ImageIO.read(this.javaClass.getResource(newIcon))
        var icon = ImageIcon(wPic)
        val name = newName
        val number = stats
    }
}

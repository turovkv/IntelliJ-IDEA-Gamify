package com.intellij.plugin.gamification.ui.toolWindow

import com.intellij.plugin.gamification.services.RewardStatisticsService
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.image.BufferedImage
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
                "",
                achievement1,
                "/icons/contract.png"
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/blue.png",
                "",
                achievement2,
                "/icons/diploma.png"
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/violet.png",
                "",
                achievement3,
                "/icons/star.png"
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/yellow.png",
                "",
                achievement4,
                "/icons/star2.png"
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/orange.png",
                "",
                achievement5,
                "/icons/cup.png"
            )
        )
        achievementsMap.add(
            Achievements(
                "/icons/red.png",
                "",
                achievement6,
                "/icons/crown.png"
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
            val wPic: BufferedImage = ImageIO.read(this.javaClass.getResource(achievementsMap[iter].emptyIcon))
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
        for ((iter, _) in achievementsMap.withIndex()) {
            val wPic: BufferedImage = ImageIO.read(this.javaClass.getResource(achievementsMap[iter].emptyIcon))
            iconsMap[iter].icon = ImageIcon(wPic)
        }
    }

    private class Achievements(newIcon: String, newName: String, stats: Int, empty: String) {
        private val wPic: BufferedImage = ImageIO.read(this.javaClass.getResource(newIcon))
        var icon = ImageIcon(wPic)
        val name = newName
        val number = stats
        val emptyIcon = empty
    }
}

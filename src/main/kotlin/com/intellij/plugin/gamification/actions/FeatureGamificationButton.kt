package com.intellij.plugin.gamification.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.plugin.gamification.services.SavedStatistics
import java.awt.BorderLayout
import java.awt.EventQueue
import java.lang.Exception
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JButton
import javax.swing.JProgressBar
import javax.swing.SwingWorker
import javax.swing.JLabel
import javax.swing.border.EmptyBorder
import java.awt.Color;
import javax.swing.UIManager;


class FeatureGamificationButton : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        EventQueue.invokeLater {
            try {
                val frame = ProgressBar()
                frame.isVisible = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class ProgressBar : JFrame() {
    val contentPane: JPanel

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

    init {
        defaultCloseOperation = HIDE_ON_CLOSE
        setBounds(0, 0, 300, 125)

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
        setContentPane(contentPane)
        val worker = ProgressWorker(progress)
        worker.execute()

    }
}



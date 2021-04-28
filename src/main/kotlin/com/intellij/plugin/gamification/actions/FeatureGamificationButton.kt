package com.intellij.plugin.gamification.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.awt.BorderLayout
import java.awt.EventQueue
import java.lang.Exception
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingWorker
import javax.swing.JLabel
import javax.swing.border.EmptyBorder


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
    private val contentPane: JPanel

    private class ProgressWorker(private val progress: JProgressBar) : SwingWorker<Void?, Int?>() {
        @Throws(Exception::class)
        override fun doInBackground(): Void? {
            for (i in LOOP_LENGTH downTo 1) {
                val progress = (100L * (LOOP_LENGTH - i) / LOOP_LENGTH).toInt()
                publish(progress)
            }
            return null
        }

        override fun process(chunks: List<Int?>) {
            progress.value = chunks[chunks.size - 1]!!
            super.process(chunks)
        }

        override fun done() {
            progress.value = 100
        }
    }

    companion object {
        private const val LOOP_LENGTH: Long = 85000000

        @JvmStatic
        fun main(args: Array<String>) {
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

    init {
        defaultCloseOperation = HIDE_ON_CLOSE
        setBounds(0, 0, 200, 75)
        contentPane = JPanel()
        contentPane.border = EmptyBorder(5, 5, 5, 5)
        contentPane.layout = BorderLayout(0, 0)
        val progress = JProgressBar()
        progress.isStringPainted = true
        contentPane.add(JLabel("Your progress: "), BorderLayout.NORTH)
        contentPane.add(progress, BorderLayout.SOUTH)
        setContentPane(contentPane)
        val worker = ProgressWorker(progress)
        worker.execute()
    }
}



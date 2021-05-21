package com.intellij.plugin.gamification.toolWindow

import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class SubscribePanel {
    val subPanel = JPanel()

    companion object {
        private const val fieldSize = 20
    }

    init {
        val lbUsername = JLabel("Find: ")
        subPanel.add(lbUsername)

        val tfUsername = JTextField(fieldSize)
        subPanel.add(tfUsername)

        val btnLogin = JButton("Subscribe")

        btnLogin.addActionListener {
            println("Try to find user:" + tfUsername.text)
        }

        subPanel.add(btnLogin)
    }
}

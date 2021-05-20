package com.intellij.plugin.gamification.widget

import javax.swing.border.LineBorder
import javax.swing.JPanel
import javax.swing.JLabel
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.swing.JButton


class SubscribePanel {
    val subPanel = JPanel()

    init {
        val lbUsername = JLabel("Find: ");
        subPanel.add(lbUsername)

        val tfUsername = JTextField(20);
        subPanel.add(tfUsername)

        val btnLogin = JButton("Subscribe")

        btnLogin.addActionListener {
            println("Try to find user:" + tfUsername.text)
        }

        subPanel.add(btnLogin)
    }
}
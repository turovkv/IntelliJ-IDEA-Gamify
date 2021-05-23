package com.intellij.plugin.gamification.toolWindow

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField

class SignInPanel(listener: ActionListener?) {

    val signPanel = JPanel()

    companion object {
        private const val fieldSize = 20
    }

    init {

        val layout = GridBagLayout()
        val gbc = GridBagConstraints()
        signPanel.layout = layout

        gbc.fill = GridBagConstraints.HORIZONTAL
        val lbLogin = JLabel("Login: ")
        gbc.gridx = 0
        gbc.gridy = 0
        signPanel.add(lbLogin, gbc)

        val tfUsername = JTextField(fieldSize)
        gbc.gridx = 1
        gbc.gridy = 0
        signPanel.add(tfUsername, gbc)

        val lbPassword = JLabel("Password: ")
        gbc.gridx = 0
        gbc.gridy = 1
        signPanel.add(lbPassword, gbc)

        val pfPassword = JPasswordField(fieldSize)
        gbc.gridx = 1
        gbc.gridy = 1
        signPanel.add(pfPassword, gbc)

        val btnLogin = JButton("Log In")

        btnLogin.addActionListener(listener)
        gbc.gridx = 1
        gbc.gridy = 2
        signPanel.add(btnLogin)
    }
}

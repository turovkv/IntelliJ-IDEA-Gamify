package com.intellij.plugin.gamification.widget;

import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JLabel
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.swing.JButton
import java.awt.GridLayout


class SignInPanel {

    val signInPanel = JPanel()

    init {
//        signInPanel.layout = GridLayout()

        val lbLogin = JLabel("Login: ")
        signInPanel.add(lbLogin, BorderLayout.NORTH)


        val tfUsername = JTextField(20)
        signInPanel.add(tfUsername, BorderLayout.NORTH)


        val lbPassword = JLabel("Password: ")
        signInPanel.add(lbPassword, BorderLayout.SOUTH)

        val pfPassword = JPasswordField(20)
        signInPanel.add(pfPassword, BorderLayout.SOUTH)

//        signInPanel.layout = GridLayout(0, 2, 10, 10)

//        signInPanel.border = LineBorder(Color.GREEN)

        val btnLogin = JButton("Login")

        btnLogin.addActionListener {
            println("login" + tfUsername.text + " password " + pfPassword.password)
        }

        signInPanel.add(btnLogin)
    }
}

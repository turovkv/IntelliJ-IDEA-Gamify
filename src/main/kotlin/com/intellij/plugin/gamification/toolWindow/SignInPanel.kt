package com.intellij.plugin.gamification.toolWindow

import com.intellij.plugin.gamification.services.NetworkService
import com.intellij.plugin.gamification.services.NetworkServiceException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.xml.bind.JAXBElement

class SignInPanel {

    val signPanel = JPanel()

    companion object {
        private const val fieldSize = 20
    }

    init {

        val lbLogin = JLabel("Login: ")
        signPanel.add(lbLogin, BorderLayout.NORTH)

        val tfUsername = JTextField(fieldSize)
        signPanel.add(tfUsername, BorderLayout.NORTH)
        val lbPassword = JLabel("Password: ")
        signPanel.add(lbPassword, BorderLayout.SOUTH)

        val pfPassword = JPasswordField(fieldSize)
        signPanel.add(pfPassword, BorderLayout.SOUTH)

        val btnLogin = JButton("Login")

        btnLogin.addActionListener {
            println("login" + tfUsername.text + " password " + pfPassword.password)
            GlobalScope.launch {
                try {
                    NetworkService.getInstance().signUp(tfUsername.text, pfPassword.password.toString())
                } catch (e: NetworkServiceException) {
                    println("Error! -> " + e.localizedMessage)
                }
            }
        }

        signPanel.add(btnLogin)
    }
}

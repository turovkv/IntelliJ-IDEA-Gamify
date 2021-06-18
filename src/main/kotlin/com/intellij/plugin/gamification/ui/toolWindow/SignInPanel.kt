package com.intellij.plugin.gamification.ui.toolWindow

import com.intellij.openapi.diagnostic.Logger
import com.intellij.plugin.gamification.services.network.ClientException
import com.intellij.plugin.gamification.services.network.NetworkService
import kotlinx.coroutines.runBlocking
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField

class SignInPanel(private val f: () -> Unit) {

    val signPanel = JPanel(GridBagLayout())
    var constraints = GridBagConstraints()

    companion object {
        private const val fieldSize = 15
        val ins = Insets(10, 10, 10, 10)
    }

    init {
        constraints.anchor = GridBagConstraints.WEST
        constraints.insets = ins

        constraints.gridx = 0
        constraints.gridy = 0
        signPanel.add(JLabel("Username:"), constraints)

        constraints.gridx = 1
        val txt = JTextField(fieldSize)
        signPanel.add(txt, constraints)

        constraints.gridx = 0
        constraints.gridy = 1
        signPanel.add(JLabel("Password:"), constraints)

        constraints.gridx = 1
        val pswd = JPasswordField(fieldSize)
        signPanel.add(pswd, constraints)

        constraints.gridx = 0
        constraints.gridy = 2
        constraints.gridwidth = 2
        constraints.anchor = GridBagConstraints.CENTER

        val btnLogin = JButton("Log In")
        val btnSignUp = JButton("Sign Up")

        btnLogin.addActionListener {
            try {
                runBlocking {
                    NetworkService.getInstance().signUp(txt.text, pswd.password.toString())
                }
                f()
            } catch (e: ClientException) {
                Logger
                    .getFactory()
                    .getLoggerInstance("Gamify")
                    .error(e)
                println(e.localizedMessage)
            }
        }

        btnSignUp.addActionListener {
            try {
                runBlocking {
                    NetworkService.getInstance().signUp(txt.text, pswd.password.toString())
                }
                f()
            } catch (e: ClientException) {
                Logger
                    .getFactory()
                    .getLoggerInstance("Gamify")
                    .error(e)
                println(e.localizedMessage)
            }
        }
        signPanel.add(btnLogin, constraints)

        constraints.gridx = 0
        constraints.gridy = 2
        constraints.gridwidth = 1
        constraints.anchor = GridBagConstraints.CENTER
        signPanel.add(btnSignUp, constraints)
    }
}

package com.intellij.plugin.gamification.toolWindow

import com.intellij.plugin.gamification.services.NetworkService
import com.intellij.plugin.gamification.services.NetworkServiceException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            GlobalScope.launch {
                try {
                    NetworkService.getInstance().subscribe(tfUsername.text.toInt())
                } catch (e: NetworkServiceException) {
                    println("Error! -> " + e.localizedMessage)
                } catch (e: NumberFormatException) {
                    println("Error! -> " + e.localizedMessage)
                }
            }
        }

        subPanel.add(btnLogin)
    }
}

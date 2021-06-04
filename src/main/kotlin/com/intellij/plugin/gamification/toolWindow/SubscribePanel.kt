package com.intellij.plugin.gamification.toolWindow

import com.intellij.openapi.ui.Splitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.table.TableView
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.ListSelectionModel

class SubscribePanel {
    val splitter = Splitter(true)
    val subPanel = JPanel()

    companion object {
        private const val fieldSize = 20
    }

    init {

        val layout = GridBagLayout()
        val gbc = GridBagConstraints()
        subPanel.layout = layout

        val lbUsername = JLabel("Find: ")
        val tfUsername = JTextField(fieldSize)
        val btnLogin = JButton("Subscribe")

        btnLogin.addActionListener {
            println("Try to find user: " + tfUsername.text)
        }

        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.gridx = 0
        gbc.gridy = 0
        subPanel.add(lbUsername, gbc)
        gbc.gridx = 1
        gbc.gridy = 0
        subPanel.add(tfUsername, gbc)
        gbc.gridx = 1
        gbc.gridy = 1
        subPanel.add(btnLogin, gbc)

        val table = TableView<Any>()
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val tablePanel = JPanel(BorderLayout())
        tablePanel.add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
        ScrollingUtil.ensureSelectionExists(table)

        splitter.isShowDividerControls = true
        splitter.firstComponent = subPanel
        splitter.secondComponent = tablePanel
    }
}

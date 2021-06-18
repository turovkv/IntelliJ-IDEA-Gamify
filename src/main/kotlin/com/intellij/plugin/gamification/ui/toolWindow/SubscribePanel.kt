package com.intellij.plugin.gamification.ui.toolWindow

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Splitter
import com.intellij.plugin.gamification.services.network.ClientException
import com.intellij.plugin.gamification.services.network.NetworkService
import com.intellij.plugin.gamification.services.network.User
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import kotlinx.coroutines.runBlocking
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
            try {
                runBlocking {
                    NetworkService.getInstance().subscribe(tfUsername.text)
                }
            } catch (e: ClientException) {
                Logger
                    .getFactory()
                    .getLoggerInstance("Gamify")
                    .error(e)
                println(e.localizedMessage)
            }
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

        val network = NetworkService.getInstance()
        val users = runBlocking { network.getAllUsers() }

        val DISPLAY_NAME: ColumnInfo<User, String> = object :
            ColumnInfo<User, String>("Name") {
            override fun valueOf(item: User?): String {
                return item?.userInfo?.displayName.toString()
            }
        }

        val POINTS: ColumnInfo<User, String> = object :
            ColumnInfo<User, String>("Points") {
            override fun valueOf(item: User?): String {
                return item?.userInfo?.progress.toString()
            }
        }
        val LEVEL: ColumnInfo<User, String> = object :
            ColumnInfo<User, String>("LVEL") {
            override fun valueOf(item: User?): String {
                return item?.userInfo?.level.toString()
            }
        }

        val COLUMNS = arrayOf<ColumnInfo<*, *>>(DISPLAY_NAME, POINTS, LEVEL)

        val table = TableView(ListTableModel(COLUMNS, users, 0))
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val tablePanel = JPanel(BorderLayout())
        tablePanel.add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
        ScrollingUtil.ensureSelectionExists(table)

        splitter.isShowDividerControls = true
        splitter.firstComponent = subPanel
        splitter.secondComponent = tablePanel
    }
}

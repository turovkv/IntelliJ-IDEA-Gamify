package com.intellij.plugin.gamification.ui.toolWindow

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Splitter
import com.intellij.plugin.gamification.services.network.ClientException
import com.intellij.plugin.gamification.services.network.NetworkService
import com.intellij.plugin.gamification.services.network.User
import com.intellij.plugin.gamification.ui.windows.NotificationWarningWindow
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import kotlinx.coroutines.runBlocking
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.net.ConnectException
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.ListSelectionModel

class SubscribePanel {
    val splitter = Splitter(true)
    val subPanel = JPanel()

    private companion object {
        const val fieldSize = 20

        val DISPLAY_NAME: ColumnInfo<User, String> = object :
            ColumnInfo<User, String>("Display Name") {
            override fun valueOf(item: User?): String {
                return item?.userInfo?.displayName.toString()
            }
        }

        val NAME: ColumnInfo<User, String> = object :
            ColumnInfo<User, String>("Name") {
            override fun valueOf(item: User?): String {
                return item?.name.toString()
            }
        }

        val POINTS: ColumnInfo<User, String> = object :
            ColumnInfo<User, String>("Points") {
            override fun valueOf(item: User?): String {
                return item?.userInfo?.progress.toString()
            }
        }
        val LEVEL: ColumnInfo<User, String> = object :
            ColumnInfo<User, String>("Level") {
            override fun valueOf(item: User?): String {
                return item?.userInfo?.level.toString()
            }
        }

        val COLUMNS = arrayOf<ColumnInfo<*, *>>(NAME, DISPLAY_NAME, POINTS, LEVEL)
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
                NotificationWarningWindow(
                    "",
                    "You cannot subscribe to an account until you are logged in."
                ).showWarning()
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

        var table = TableView<Any>()
        var tablePanel = JPanel()
        try {
            val network = NetworkService.getInstance()
            val users = runBlocking {
                network.getAllUsers()
            }

            tablePanel = JPanel(BorderLayout())

            table = TableView(ListTableModel(COLUMNS, users, 0))
            table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

            tablePanel.add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
        } catch (e: ClientException) {
            println(e.localizedMessage)
            tablePanel.add(JLabel("Can't see the table. No connection."))
        } catch (e: ConnectException) {
            println(e.localizedMessage)
            tablePanel.add(JLabel("Can't see the table. No connection."))
        }

        ScrollingUtil.ensureSelectionExists(table)

        splitter.isShowDividerControls = true
        splitter.firstComponent = subPanel
        splitter.secondComponent = tablePanel
    }
}

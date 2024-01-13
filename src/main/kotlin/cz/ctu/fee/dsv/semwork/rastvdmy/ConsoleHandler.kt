package cz.ctu.fee.dsv.semwork.rastvdmy

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

/**
 * This class handles console input and output for a node in a distributed system.
 * It implements the Runnable interface to allow it to be run in a separate thread.
 */
class ConsoleHandler(private val myNode: Node) : Runnable {
    private var reading = true
    private var reader: BufferedReader? = null
    private val err: PrintStream = System.err

    init {
        reader = BufferedReader(InputStreamReader(System.`in`))
    }

    /**
     * Parses a command line input and executes the corresponding command.
     *
     * @param commandline The command line input to parse.
     */
    private fun parseCommandline(commandline: String) {
        when (commandline) {
            "h" -> {
                myNode.sendHelloToBoth()
            }
            "m" -> {
                println("Enter the port of the node to which you want to send a message:")
                val port = reader!!.readLine()
                println("Enter the message:")
                val message = reader!!.readLine()
                myNode.sendMessageByPort(port.toInt(), message)
            }
            "s" -> {
                myNode.printStatus()
            }
            "?" -> {
                print("? - showing all available commands\n")
                print("h - send Hello message to both neighbours\n")
                print("m - send message to a node\n")
                print("s - print node status\n")
            }
            else -> {
                // do nothing
                print("Unrecognized command.")
            }
        }
    }

    /**
     * The main loop of the ConsoleHandler.
     * It reads command line inputs and parses them until an error occurs or the reading flag is set to false.
     */
    override fun run() {
        var commandline: String
        while (reading) {
            print("\ncmd > ")
            try {
                commandline = reader!!.readLine()
                parseCommandline(commandline)
            } catch (e: IOException) {
                err.println("ConsoleHandler - error in reading console input.")
                e.printStackTrace()
                reading = false
            }
        }
        println("Closing ConsoleHandler.")
    }
}
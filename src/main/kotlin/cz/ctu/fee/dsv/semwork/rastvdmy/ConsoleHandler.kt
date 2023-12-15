package cz.ctu.fee.dsv.semwork.rastvdmy

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream

class ConsoleHandler(private val myNode: Node) : Runnable {
    private var reading = true
    private var reader: BufferedReader? = null
    private val err: PrintStream = System.err


    init {
        reader = BufferedReader(InputStreamReader(System.`in`))
    }


    private fun parseCommandline(commandline: String) {
        when (commandline) {
            "h" -> {
                myNode.sendHelloToBoth()
            }
            "s" -> {
                myNode.printStatus()
            }
            "?" -> {
                print("? - this help")
                print("h - send Hello message to Next neighbour")
                print("s - print node status")
            }
            else -> {
                // do nothing
                print("Unrecognized command.")
            }
        }
    }


    override fun run() {
        var commandline: String
        while (reading) {
            print("\ncmd > ")
            try {
                commandline = reader!!.readLine()
                parseCommandline(commandline)
            } catch (e: IOException) {
                err.println("ConsoleHandler - error in rading console input.")
                e.printStackTrace()
                reading = false
            }
        }
        println("Closing ConsoleHandler.")
    }
}
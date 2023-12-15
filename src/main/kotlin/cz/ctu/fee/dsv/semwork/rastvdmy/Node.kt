package cz.ctu.fee.dsv.semwork.rastvdmy

import cz.ctu.fee.dsv.semwork.rastvdmy.base.Address
import cz.ctu.fee.dsv.semwork.rastvdmy.base.DSNeighbours
import cz.ctu.fee.dsv.semwork.rastvdmy.base.NodeCommands
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject
import kotlin.system.exitProcess

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

class Node(args: Array<String>) : Runnable {

    // Node state
    var state: State = State.NOT_INVOLVED

    // Initial configuration from commandline
    private var nickname = "Unknown"
    private var myIP = "127.0.0.1"
    private var myPort = 2010
    private var otherNodeIP = "127.0.0.1"
    private var otherNodePort = 2010

    // Node Id
    var nodeId: Long = 0
    var address: Address? = null
        private set
    var neighbours: DSNeighbours? = null
        private set
    var messageReceiver: NodeCommands? = null
        private set
    var commHub: CommunicationHub? = null
        private set
    private var myConsoleHandler: ConsoleHandler? = null

    private var repairInProgress: Boolean = false

    init {
        // handle commandline arguments
        when (args.size) {
            3 -> {
                nickname = args[0]
                otherNodeIP = args[1]
                myIP = otherNodeIP
                otherNodePort = args[2].toInt()
                myPort = otherNodePort
            }
            5 -> {
                nickname = args[0]
                myIP = args[1]
                myPort = args[2].toInt()
                otherNodeIP = args[3]
                otherNodePort = args[4].toInt()
            }
            else -> {
                // something is wrong - use default values
                System.err.println("Wrong number of commandline parameters - using default values.")
            }
        }
    }

    private fun generateId(myIP: String, port: Int): Long {
        // generates <port><IPv4_dec1><IPv4_dec2><IPv4_dec3><IPv4_dec4>
        val array = myIP.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var id: Long = 0
        var temp: Long
        for (s in array) {
            try {
                temp = s.toLong()
                id = (id * 1000) + temp
            } catch (e: NumberFormatException) {
                // Handle the case where parsing fails (invalid IPv4 address)
                id = 666000666000L
                break
            }
        }
        id += port * 1000000000000L
        return id
    }

    private fun startMessageReceiver(): NodeCommands? {
        if (address == null) {
            System.err.println("Message listener - myAddress is null.")
            return null
        }
        System.setProperty("java.rmi.server.hostname", address!!.hostname)

        var msgReceiver: NodeCommands? = null
        try {
            msgReceiver = MessageReceiver(this)

            // Create instance of a remote object and its skeleton
            val skeleton = UnicastRemoteObject.exportObject(msgReceiver, 40000 + address!!.port) as NodeCommands

            // Create registry and (re)register object name and skeleton in it
            val registry = LocateRegistry.createRegistry(address!!.port)
            registry.rebind(COMM_INTERFACE_NAME, skeleton)
        } catch (e: Exception) {
            // Something is wrong ...
            System.err.println("Message listener - something is wrong: " + e.message)
        }
        println("Message listener is started ...")

        return msgReceiver
    }


    override fun toString(): String {
        return "Node[id:'" + nodeId + "', " +
                "nick:'" + nickname + "', " +
                "myIP:'" + myIP + "', " +
                "myPort:'" + myPort + "', " +
                "otherNodeIP:'" + otherNodeIP + "', " +
                "otherNodePort:'" + otherNodePort + "']"
    }


    fun printStatus() {
        println("Status: $this with address $address")
        println("    with neighbours $neighbours")
    }


    override fun run() {
        nodeId = generateId(myIP, myPort)
        address = Address(myIP, myPort)
        neighbours = DSNeighbours(address!!)
        printStatus()
        messageReceiver = startMessageReceiver()
        commHub = CommunicationHub(this)
        myConsoleHandler = ConsoleHandler(this)
        // JOIN
        run {
            try {
                val tmpNode = commHub!!.getRMIProxy(Address(otherNodeIP, otherNodePort))
                this.neighbours = tmpNode!!.join(this.address)
                commHub!!.setActNeighbours(this.neighbours)
            } catch (e: RemoteException) {
                e.printStackTrace()
                exitProcess(1)
            }
            println("Neighbours after JOIN " + this.neighbours)
        }
        Thread(myConsoleHandler).start()
    }


    private fun repairTopology() {
        if (!repairInProgress) {
            repairInProgress = true
            run {
                try {
                    messageReceiver!!.nodeMissing(neighbours!!.next)
                } catch (e: RemoteException) {
                    // this should not happen
                    e.printStackTrace()
                }
                println("Topology was repaired " + this.neighbours)
            }
            repairInProgress = false

            // test leader
            try {
                commHub!!.leader!!.hello()
            } catch (e: RemoteException) {
                // Leader is dead -> start Election with a new chosen candidate to become a leader
                try {
                    messageReceiver!!.election(-1)
                } catch (ex: RemoteException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    fun sendHelloToBoth() {
        println("Sending Hello to both neighbours")
        try {
            println("Sending Hello to ${commHub!!.next}}")
            commHub!!.next!!.hello()
            println("Sending Hello to ${commHub!!.prev}}")
            commHub!!.prev!!.hello()
        } catch (e: RemoteException) {
            repairTopology()
        }
    }

    companion object {
        // Using logger is strongly recommended (log4j, ...)
        // Name of our RMI "service"
        const val COMM_INTERFACE_NAME: String = "DSVNode"

        // This Node
        private var thisNode: Node? = null

        @JvmStatic
        fun main(args: Array<String>) {
            thisNode = Node(args)
            thisNode!!.run()
        }
    }
}

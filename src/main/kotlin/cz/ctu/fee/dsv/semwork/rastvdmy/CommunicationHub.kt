package cz.ctu.fee.dsv.semwork.rastvdmy

import cz.ctu.fee.dsv.semwork.rastvdmy.base.Address
import cz.ctu.fee.dsv.semwork.rastvdmy.base.DSNeighbours
import cz.ctu.fee.dsv.semwork.rastvdmy.base.NodeCommands
import java.rmi.NotBoundException
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

class CommunicationHub(private val node: Node?) {
    private var actNeighbours: DSNeighbours
    private var myAddress: Address? = null
    private var myMessageReceiver: NodeCommands? = null

    init {
        if (node == null) throw NullPointerException("CommunicationHub - node is null.")
        this.myAddress = node.proxiedAddress
        this.actNeighbours = node.neighbours!!
        this.myMessageReceiver = node.messageReceiver
    }

    @get:Throws(RemoteException::class)
    val right: NodeCommands?
        get() = getRMIProxy(actNeighbours.right)


    @get:Throws(RemoteException::class)
    val nNext: NodeCommands?
        get() = getRMIProxy(actNeighbours.nNext)


    @get:Throws(RemoteException::class)
    val left: NodeCommands?
        get() = getRMIProxy(actNeighbours.left)


    @get:Throws(RemoteException::class)
    val leader: NodeCommands?
        get() = getRMIProxy(actNeighbours.leader)

    @get:Throws(RemoteException::class)
    val current: NodeCommands?
        get() = getRMIProxy(myAddress!!)

    @Throws(RemoteException::class)
    fun getRMIProxy(proxiedAddress: Address): NodeCommands? {
        if (proxiedAddress.compareTo(myAddress!!) == 0) {
            return myMessageReceiver
        }
        else {
            try {
                val registry = LocateRegistry.getRegistry(proxiedAddress.hostname, proxiedAddress.port)
                return registry.lookup(Node.COMM_INTERFACE_NAME) as NodeCommands
            } catch (e: Exception) {
                // transitive RM exception
                node!!.repairTopology(proxiedAddress)
                return null
            }
        }
    }

    fun setActNeighbours(actNeighbours: DSNeighbours?) {
        if (actNeighbours != null) {
            this.actNeighbours = actNeighbours
        }
    }
}
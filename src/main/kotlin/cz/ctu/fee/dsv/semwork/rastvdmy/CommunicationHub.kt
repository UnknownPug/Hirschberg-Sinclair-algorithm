package cz.ctu.fee.dsv.semwork.rastvdmy

import cz.ctu.fee.dsv.semwork.rastvdmy.base.Address
import cz.ctu.fee.dsv.semwork.rastvdmy.base.DSNeighbours
import cz.ctu.fee.dsv.semwork.rastvdmy.base.NodeCommands
import java.rmi.NotBoundException
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

class CommunicationHub(node: Node?) {
    private var actNeighbours: DSNeighbours? = null
    private var myAddress: Address? = null
    private var myMessageReceiver: NodeCommands? = null


    init {
        if (node == null) throw NullPointerException("CommunicationHub - node is null.")
        this.myAddress = node.address
        this.actNeighbours = node.neighbours
        this.myMessageReceiver = node.messageReceiver
    }

    @get:Throws(RemoteException::class)
    val next: NodeCommands?
        get() = getRMIProxy(actNeighbours!!.next)


    @get:Throws(RemoteException::class)
    val nNext: NodeCommands?
        get() = getRMIProxy(actNeighbours!!.nNext)


    @get:Throws(RemoteException::class)
    val prev: NodeCommands?
        get() = getRMIProxy(actNeighbours!!.prev)


    @get:Throws(RemoteException::class)
    val leader: NodeCommands?
        get() = getRMIProxy(actNeighbours!!.leader)


    @Throws(RemoteException::class)
    fun getRMIProxy(address: Address): NodeCommands? {
        if (address.compareTo(myAddress!!) == 0) return myMessageReceiver
        else {
            try {
                val registry = LocateRegistry.getRegistry(address.hostname, address.port)
                return registry.lookup(Node.COMM_INTERFACE_NAME) as NodeCommands
            } catch (nbe: NotBoundException) {
                // transitive RM exception
                throw RemoteException(nbe.message)
            }
        }
    }

    fun setActNeighbours(actNeighbours: DSNeighbours?) {
        this.actNeighbours = actNeighbours
    }
}
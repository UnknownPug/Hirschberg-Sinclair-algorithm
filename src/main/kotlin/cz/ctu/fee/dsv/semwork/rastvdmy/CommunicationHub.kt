package cz.ctu.fee.dsv.semwork.rastvdmy

import cz.ctu.fee.dsv.semwork.rastvdmy.base.Address
import cz.ctu.fee.dsv.semwork.rastvdmy.base.DSNeighbours
import cz.ctu.fee.dsv.semwork.rastvdmy.base.NodeCommands
import java.rmi.NotBoundException
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

/**
 * This class manages the communication between nodes in a distributed system.
 * It provides methods to get the RMI proxy of a node and to set the current neighbours of a node.
 */
class CommunicationHub(private val node: Node?) {
    private var actNeighbours: DSNeighbours
    private var myAddress: Address? = null
    private var myMessageReceiver: NodeCommands? = null

    init {
        if (node == null) throw NullPointerException("CommunicationHub - node is null.")
        this.myAddress = node.address
        this.actNeighbours = node.neighbours!!
        this.myMessageReceiver = node.messageReceiver
    }

    /**
     * Gets the RMI proxy of the right neighbour.
     *
     * @return The RMI proxy of the right neighbour.
     * @throws RemoteException If a remote access error occurs.
     */
    @get:Throws(RemoteException::class)
    val right: NodeCommands?
        get() = getRMIProxy(actNeighbours.right)

    /**
     * Gets the RMI proxy of the next neighbour.
     *
     * @return The RMI proxy of the next neighbour.
     * @throws RemoteException If a remote access error occurs.
     */
    @get:Throws(RemoteException::class)
    val nNext: NodeCommands?
        get() = getRMIProxy(actNeighbours.nNext)

    /**
     * Gets the RMI proxy of the left neighbour.
     *
     * @return The RMI proxy of the left neighbour.
     * @throws RemoteException If a remote access error occurs.
     */
    @get:Throws(RemoteException::class)
    val left: NodeCommands?
        get() = getRMIProxy(actNeighbours.left)

    /**
     * Gets the RMI proxy of the leader.
     *
     * @return The RMI proxy of the leader.
     * @throws RemoteException If a remote access error occurs.
     */
    @get:Throws(RemoteException::class)
    val leader: NodeCommands?
        get() = getRMIProxy(actNeighbours.leader)

    /**
     * Gets the RMI proxy of the current node.
     *
     * @return The RMI proxy of the current node.
     * @throws RemoteException If a remote access error occurs.
     */
    @get:Throws(RemoteException::class)
    val current: NodeCommands?
        get() = getRMIProxy(myAddress!!)

    /**
     * Gets the RMI proxy of a node with a given address.
     *
     * @param proxiedAddress The address of the node.
     * @return The RMI proxy of the node.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun getRMIProxy(proxiedAddress: Address): NodeCommands? {
        if (proxiedAddress.compareTo(myAddress!!) == 0) {
            return myMessageReceiver
        } else {
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

    /**
     * Sets the current neighbours of a node.
     *
     * @param actNeighbours The new neighbours of the node.
     */
    fun setActNeighbours(actNeighbours: DSNeighbours?) {
        if (actNeighbours != null) {
            this.actNeighbours = actNeighbours
        }
    }
}
package cz.ctu.fee.dsv.semwork.rastvdmy.base

import java.rmi.Remote
import java.rmi.RemoteException

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

/**
 * This interface defines the commands that a node in a distributed system can execute.
 * It extends the Remote interface to allow these commands to be called from a remote machine.
 */
interface NodeCommands : Remote {

    /**
     * Joins a node to the distributed system.
     *
     * @param addr The address of the node to join.
     * @return The neighbours of the joined node.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun join(addr: Address?): DSNeighbours?

    /**
     * Changes the next neighbour of a node.
     *
     * @param addr The address of the new next neighbour.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun changeNNext(addr: Address?)

    /**
     * Changes the previous neighbour of a node.
     *
     * @param addr The address of the new previous neighbour.
     * @return The address of the changed node.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun changePrev(addr: Address?): Address?

    /**
     * Notifies that a node is missing in the distributed system.
     *
     * @param addr The address of the missing node.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun nodeMissing(addr: Address?)

    /**
     * Starts an election in the distributed system.
     *
     * @param id The id of the node starting the election.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun election(id: Long)

    /**
     * Sends a candidature for the election.
     *
     * @param leaderId The id of the candidate.
     * @param minDepth The minimum depth of the candidate.
     * @param maxDepth The maximum depth of the candidate.
     * @param senderAddress The address of the sender.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun candidature(leaderId: Long, minDepth: Int, maxDepth: Int, senderAddress: Address)

    /**
     * Sends a response to a candidature.
     *
     * @param isPositiveResponse Whether the response is positive or not.
     * @param id The id of the responder.
     * @param senderAddress The address of the sender.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun response(isPositiveResponse: Boolean, id: Long, senderAddress: Address)

    /**
     * Notifies that a node has been elected.
     *
     * @param winner The id of the winner.
     * @param winnerAddress The address of the winner.
     * @param senderAddress The address of the sender.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun elected(winner: Long, winnerAddress: Address, senderAddress: Address)

    /**
     * Sends a message to a node.
     *
     * @param toNickName The nickname of the recipient.
     * @param fromNickName The nickname of the sender.
     * @param message The message to send.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun sendMessage(toNickName: String?, fromNickName: String?, message: String?)

    /**
     * Registers a node in the distributed system.
     *
     * @param nickName The nickname of the node.
     * @param addr The address of the node.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun register(nickName: String?, addr: Address?)

    /**
     * Sends a hello message to a node.
     *
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    fun hello()
}
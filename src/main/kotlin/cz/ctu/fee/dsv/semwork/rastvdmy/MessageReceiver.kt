package cz.ctu.fee.dsv.semwork.rastvdmy

import cz.ctu.fee.dsv.semwork.rastvdmy.base.Address
import cz.ctu.fee.dsv.semwork.rastvdmy.base.DSNeighbours
import cz.ctu.fee.dsv.semwork.rastvdmy.base.NodeCommands
import java.rmi.RemoteException

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

/**
 * This class handles the receiving of messages for a node in a distributed system.
 * It implements the NodeCommands interface to define the commands that a node can execute.
 */
class MessageReceiver(node: Node?) : NodeCommands {
    private var myNode: Node? = null
    private var nResp = 0
    private var respOK = true

    // Winner ID
    private var winner: Long = 0

    init {
        this.myNode = node
    }

    /**
     * Joins a node to the distributed system.
     *
     * @param addr The address of the node to join.
     * @return The neighbours of the joined node.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    override fun join(addr: Address?): DSNeighbours? {
        println("JOIN was called ...")
        if (addr!!.compareTo(myNode!!.address!!) == 0) {
            println("I am the first and leader")
            return myNode!!.neighbours
        } else {
            println("Someone is joining ...")
            val myNeighbours = myNode!!.neighbours
            val myInitialNext = Address(myNeighbours!!.right) // because of 2 nodes config
            val myInitialPrev = Address(myNeighbours.left) // because of 2 nodes config
            val tmpNeighbours = DSNeighbours(
                myNeighbours.right,
                myNeighbours.nNext,
                myNode!!.address!!,
                myNeighbours.leader
            )
            // to my next send msg ChPrev to addr
            myNode!!.commHub!!.right!!.changePrev(addr)
            // to my prev send msg ChNNext addr
            myNode!!.commHub!!.getRMIProxy(myInitialPrev)!!.changeNNext(addr)
            tmpNeighbours.nNext = myNeighbours.nNext
            // handle myself
            myNeighbours.nNext = myInitialNext
            myNeighbours.right = addr
            return tmpNeighbours
        }
    }

    /**
     * Changes the next neighbour of a node.
     *
     * @param addr The address of the new next neighbour.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    override fun changeNNext(addr: Address?) {
        println("ChangeNnext was called ...")
        myNode!!.neighbours!!.nNext = addr!!
    }

    /**
     * Changes the previous neighbour of a node.
     *
     * @param addr The address of the new previous neighbour.
     * @return The address of the changed node.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    override fun changePrev(addr: Address?): Address {
        println("ChangePrev was called ...")
        myNode!!.neighbours!!.left = addr!!
        return myNode!!.neighbours!!.right
    }

    /**
     * Notifies that a node is missing in the distributed system.
     *
     * @param addr The address of the missing node.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    override fun nodeMissing(addr: Address?) {
        println("NodeMissing was called with $addr")
        if (addr!!.compareTo(myNode!!.neighbours!!.right) == 0) {
            // It's for me
            val myNeighbours = myNode!!.neighbours
            // to my nnext send msg ChPrev with myaddr -> my nnext = next
            myNeighbours!!.right = myNeighbours.nNext
            myNeighbours.nNext = myNode!!.commHub!!.nNext!!.changePrev(myNode!!.address)!!
            // to my prev send msg ChNNext to my.next
            myNode!!.commHub!!.left!!.changeNNext(myNeighbours.right)
            println("NodeMissing DONE")
            // test leader
            testingLeader(addr)
        } else {
            // send to the next node
            myNode!!.commHub!!.right!!.nodeMissing(addr)
        }
    }

    /**
     * Tests if the leader of the distributed system is missing.
     * If the leader is missing, it starts an election process to elect a new leader.
     *
     * @param missingAddress The address of the missing node.
     */
    private fun testingLeader(missingAddress: Address) {
        println("Testing leader")
        if (this.myNode!!.neighbours!!.leader.compareTo(missingAddress) == 0) {
//            readln() // Waiting for the user to press entering (blocking the nodes for an election process)
            println("Leader is dead -> start Candidature with a new chosen candidate to become a leader")
            election(myNode!!.nodeId)
        }
    }

    /**
     * Starts an election in the distributed system.
     *
     * @param id The id of the node starting the election.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    override fun election(id: Long) {
        myNode!!.state = State.CANDIDATE
        var maxDepth = 1
        println("Election was called with id $id, state is ${myNode!!.state}, maxDepth is $maxDepth.")
        while (myNode!!.state == State.CANDIDATE) {
            nResp = 0
            respOK = true
            myNode!!.commHub!!.left!!.candidature(id, 0, maxDepth, myNode!!.address!!)
            myNode!!.commHub!!.right!!.candidature(id, 0, maxDepth, myNode!!.address!!)
            waitUntil { nResp == 2 || (winner == myNode!!.nodeId) }
            synchronized(this) {
                if (!respOK) {
                    myNode!!.state = State.LOST
                }
            }
            maxDepth *= 2
        }
    }

    private fun waitUntil(function: () -> Boolean) {
        while (!function()) {
            Thread.sleep(250)
            print("$nResp")
        }
    }

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
    override fun candidature(leaderId: Long, minDepth: Int, maxDepth: Int, senderAddress: Address) {
        val calledNodeId = myNode!!.nodeId
        if (calledNodeId > leaderId) {
            myNode!!.commHub!!.getRMIProxy(senderAddress)!!.response(false, leaderId, senderAddress)
            if (myNode!!.state == State.NOT_INVOLVED) {
                println("Node $calledNodeId was not involved in election before. Starting new election ...")
                election(calledNodeId)
            }
        } else if (calledNodeId < leaderId) {
            myNode!!.state = State.LOST
            val newMinDepth = minDepth + 1
            if (newMinDepth < maxDepth) {
                sendPassCandidature(leaderId, newMinDepth, maxDepth, senderAddress)
            } else {
                myNode!!.commHub!!.getRMIProxy(senderAddress)!!.response(true, leaderId, myNode!!.address!!)
            }
        }
        // If calledNodeId is equals to leaderId
        else {
            if (myNode!!.state != State.ELECTED) {
                myNode!!.state = State.ELECTED
            }
            println("Node $calledNodeId is elected. Setting winner to $calledNodeId and sending elected to next or prev ...")
            winner = calledNodeId
            // Notify the next and prev that I am the winner
            getProxyToPassMessage(senderAddress).elected(winner, myNode!!.address!!, myNode!!.address!!)
            myNode!!.neighbours!!.leader = myNode!!.address!!
        }
    }

    /**
     * Sends a response to a candidature.
     *
     * @param isPositiveResponse Whether the response is positive or not.
     * @param id The id of the responder.
     * @param senderAddress The address of the sender.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    override fun response(isPositiveResponse: Boolean, id: Long, senderAddress: Address) {
        if (myNode!!.nodeId == id) {
            nResp++
            respOK = respOK && isPositiveResponse // If it gets true and if it gets true
        } else {
            getProxyToPassMessage(senderAddress).response(isPositiveResponse, id, myNode!!.address!!)
        }
    }

    /**
     * Notifies that a node has been elected.
     *
     * @param winner The id of the winner.
     * @param winnerAddress The address of the winner.
     * @param senderAddress The address of the sender.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    override fun elected(winner: Long, winnerAddress: Address, senderAddress: Address) {
        if (winner != myNode!!.nodeId) {
            getProxyToPassMessage(senderAddress).elected(winner, winnerAddress, myNode!!.address!!)
            myNode!!.neighbours!!.leader = winnerAddress
            this.winner = winner
            myNode!!.state = State.NOT_INVOLVED
        }
    }

    /**
     * Returns the RMI proxy of the node to which the message should be passed.
     * If the sender is the right neighbour, it returns the left neighbour's proxy, and vice versa.
     *
     * @param senderAddress The address of the sender node.
     * @return The RMI proxy of the node to which the message should be passed.
     */
    private fun getProxyToPassMessage(senderAddress: Address): NodeCommands {
        return if (senderAddress.compareTo(myNode!!.neighbours!!.right) == 0) {
            myNode!!.commHub!!.getRMIProxy(myNode!!.neighbours!!.left)!!
        } else {
            myNode!!.commHub!!.getRMIProxy(myNode!!.neighbours!!.right)!!
        }
    }

    /**
     * Sends a candidature message to the appropriate node during an election process.
     * The message is passed to the node returned by the getProxyToPassMessage method.
     *
     * @param leaderId The id of the candidate.
     * @param newMinDepth The new minimum depth of the candidate.
     * @param maxDepth The maximum depth of the candidate.
     * @param senderAddress The address of the sender node.
     */
    private fun sendPassCandidature(leaderId: Long, newMinDepth: Int, maxDepth: Int, senderAddress: Address) {
        getProxyToPassMessage(senderAddress).candidature(leaderId, newMinDepth, maxDepth, myNode!!.address!!)
    }

    /**
     * Sends a message to a node.
     *
     * @param fromNickName The nickname of the sender.
     * @param message The message to send.
     * @throws RemoteException If a remote access error occurs.
     */
    @Throws(RemoteException::class)
    override fun sendMessage(fromNickName: String?, message: String?) {
        println("Message was sent from $fromNickName: $message")
    }

    @Throws(RemoteException::class)
    override fun register(nickName: String?, addr: Address?) {
    }

    /**
    * Prints hello was called to the console after being called.
    */
    @Throws(RemoteException::class)
    override fun hello() {
        println("Hello was called ...")
    }

    @Throws(RemoteException::class)
    override fun receiveMessage(message: String) {
        println("Message was received: $message")
    }

    @Throws(RemoteException::class)
    override fun notifyMessageSent(sender: String, receiver: String, message: String) {
        println("Message was sent from $sender to $receiver: $message")
    }
}

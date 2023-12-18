package cz.ctu.fee.dsv.semwork.rastvdmy

import cz.ctu.fee.dsv.semwork.rastvdmy.base.Address
import cz.ctu.fee.dsv.semwork.rastvdmy.base.DSNeighbours
import cz.ctu.fee.dsv.semwork.rastvdmy.base.NodeCommands
import java.rmi.RemoteException

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

class MessageReceiver(node: Node?) : NodeCommands {
    private var myNode: Node? = null
    private var nResp = 0
    private var respOK = true
    private var winner: Long = 0

    init {
        this.myNode = node
    }

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


    @Throws(RemoteException::class)
    override fun changeNNext(addr: Address?) {
        println("ChangeNnext was called ...")
        myNode!!.neighbours!!.nNext = addr!!
    }


    @Throws(RemoteException::class)
    override fun changePrev(addr: Address?): Address {
        println("ChangePrev was called ...")
        myNode!!.neighbours!!.left = addr!!
        return myNode!!.neighbours!!.right
    }


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
        } else {
            // send to the next node
            myNode!!.commHub!!.right!!.nodeMissing(addr)
        }
    }

    @Synchronized
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
            waitUntil { nResp == 2 }
            if (!respOK) {
                myNode!!.state = State.LOST
                break // Declares himself as LOST and does nothing more
            }
            maxDepth *= 2
        }
    }

    private fun waitUntil(function: () -> Boolean) {
        while (!function()) {
            Thread.sleep(100)
        }
    }

    @Synchronized
    @Throws(RemoteException::class)
    override fun candidature(leaderId: Long, minDepth: Int, maxDepth: Int, senderAddress: Address) {
        val calledNodeId = myNode!!.nodeId

        println("Candidature was called with id $leaderId")
        if (calledNodeId > leaderId) {
            println("Leader $leaderId is not a candidate. Sending response to leader ...")
            myNode!!.commHub!!.getRMIProxy(senderAddress)!!
                .response(false, leaderId, senderAddress) // FIXME: LOOPING HERE!!!
            if (myNode!!.state == State.NOT_INVOLVED) {
                println("Node $calledNodeId was not involved in election before. Starting new election ...")
                election(calledNodeId)
            }
        }
        if (calledNodeId < leaderId) {
            myNode!!.state = State.LOST
            val newMinDepth = minDepth + 1
            println("New minimal depth is $newMinDepth")
            if (newMinDepth < maxDepth) {
                sendPassCandidature(leaderId, newMinDepth, maxDepth, senderAddress)
            } else {
                myNode!!.commHub!!.getRMIProxy(senderAddress)!!.response(true, leaderId, myNode!!.address!!)
            }
        }
        if (leaderId == calledNodeId) {
            if (myNode!!.state != State.ELECTED) {
                println("STATE ELECTED")
                myNode!!.state = State.ELECTED
            }
            winner = calledNodeId
            // Notify the next and prev that I am the winner
            getProxyToPassMessage(senderAddress).elected(winner, myNode!!.address!!)
        }
    }

    @Synchronized
    @Throws(RemoteException::class)
    override fun response(b: Boolean, id: Long, senderAddress: Address) {
        println("Response was called with b $b, id $id and sender address $senderAddress")
        if (myNode!!.nodeId == id) {
            nResp++
            respOK = respOK && b // If it gets true and if it gets true
        } else {
            getProxyToPassMessage(senderAddress)
        }
    }

    @Synchronized
    @Throws(RemoteException::class)
    override fun elected(winner: Long, senderAddress: Address) {
        println("Elected was called with id $winner")
        if (winner != myNode!!.nodeId) {
            getProxyToPassMessage(senderAddress).elected(winner, myNode!!.address!!)
            this.winner = winner
            myNode!!.state = State.NOT_INVOLVED
        }
    }

    private fun getProxyToPassMessage(senderAddress: Address): NodeCommands {
        return if (senderAddress == myNode!!.neighbours!!.right) {
            myNode!!.commHub!!.getRMIProxy(myNode!!.neighbours!!.left)!!
        } else {
            myNode!!.commHub!!.getRMIProxy(myNode!!.neighbours!!.right)!!
        }
    }

    private fun sendPassCandidature(leaderId: Long, newMinDepth: Int, maxDepth: Int, senderAddress: Address) {
        getProxyToPassMessage(senderAddress).candidature(leaderId, newMinDepth, maxDepth, myNode!!.address!!)
    }

    @Throws(RemoteException::class)
    override fun sendMessage(toNickName: String?, fromNickName: String?, message: String?) {
    }

    @Throws(RemoteException::class)
    override fun register(nickName: String?, addr: Address?) {
    }

    @Throws(RemoteException::class)
    override fun hello() {
        println("Hello was called ...")
    }
}

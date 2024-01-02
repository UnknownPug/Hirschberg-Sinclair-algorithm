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

    // Winner ID
    private var winner: Long = 0

    init {
        this.myNode = node
    }

    @Throws(RemoteException::class)
    override fun join(addr: Address?): DSNeighbours? {
        println("JOIN was called ...")
        if (addr!!.compareTo(myNode!!.proxiedAddress!!) == 0) {
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
                myNode!!.proxiedAddress!!,
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
            myNeighbours.nNext = myNode!!.commHub!!.nNext!!.changePrev(myNode!!.proxiedAddress)!!
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

    private fun testingLeader(missingAddress: Address) {
        println("Testing leader")
        if (this.myNode!!.neighbours!!.leader.compareTo(missingAddress) == 0) {
            println("Leader is dead -> start Candidature with a new chosen candidate to become a leader")
            election(myNode!!.nodeId)
        }
    }

    @Throws(RemoteException::class)
    override fun election(id: Long) {
        myNode!!.state = State.CANDIDATE
        var maxDepth = 1
        println("Election was called with id $id, state is ${myNode!!.state}, maxDepth is $maxDepth.")
        while (myNode!!.state == State.CANDIDATE) {
            nResp = 0
            respOK = true
            myNode!!.commHub!!.left!!.candidature(id, 0, maxDepth, myNode!!.proxiedAddress!!)
            myNode!!.commHub!!.right!!.candidature(id, 0, maxDepth, myNode!!.proxiedAddress!!)
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
                myNode!!.commHub!!.getRMIProxy(senderAddress)!!.response(true, leaderId, myNode!!.proxiedAddress!!)
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
            getProxyToPassMessage(senderAddress).elected(winner, myNode!!.proxiedAddress!!, myNode!!.proxiedAddress!!)
            myNode!!.neighbours!!.leader = myNode!!.proxiedAddress!!
        }
    }

    @Throws(RemoteException::class)
    override fun response(isPositiveResponse: Boolean, id: Long, senderAddress: Address) {
        if (myNode!!.nodeId == id) {
            nResp++
            respOK = respOK && isPositiveResponse // If it gets true and if it gets true
        } else {
            getProxyToPassMessage(senderAddress).response(isPositiveResponse, id, myNode!!.proxiedAddress!!)
        }
    }

    @Throws(RemoteException::class)
    override fun elected(winner: Long, winnerAddress: Address, senderAddress: Address) {
        if (winner != myNode!!.nodeId) {
            getProxyToPassMessage(senderAddress).elected(winner, winnerAddress, myNode!!.proxiedAddress!!)
            myNode!!.neighbours!!.leader = winnerAddress
            this.winner = winner
            myNode!!.state = State.NOT_INVOLVED
        }
    }

    private fun getProxyToPassMessage(senderAddress: Address): NodeCommands {
        return if (senderAddress.compareTo(myNode!!.neighbours!!.right) == 0) {
            myNode!!.commHub!!.getRMIProxy(myNode!!.neighbours!!.left)!!
        } else {
            myNode!!.commHub!!.getRMIProxy(myNode!!.neighbours!!.right)!!
        }
    }

    private fun sendPassCandidature(leaderId: Long, newMinDepth: Int, maxDepth: Int, senderAddress: Address) {
        getProxyToPassMessage(senderAddress).candidature(leaderId, newMinDepth, maxDepth, myNode!!.proxiedAddress!!)
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

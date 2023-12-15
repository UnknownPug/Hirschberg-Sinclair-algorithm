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
            val myInitialNext = Address(myNeighbours!!.next) // because of 2 nodes config
            val myInitialPrev = Address(myNeighbours.prev) // because of 2 nodes config
            val tmpNeighbours = DSNeighbours(
                myNeighbours.next,
                myNeighbours.nNext,
                myNode!!.address!!,
                myNeighbours.leader
            )
            // to my next send msg ChPrev to addr
            myNode!!.commHub!!.next!!.changePrev(addr)
            // to my prev send msg ChNNext addr
            myNode!!.commHub!!.getRMIProxy(myInitialPrev)!!.changeNNext(addr)
            tmpNeighbours.nNext = myNeighbours.nNext
            // handle myself
            myNeighbours.nNext = myInitialNext
            myNeighbours.next = addr
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
        myNode!!.neighbours!!.prev = addr!!
        return myNode!!.neighbours!!.next
    }


    @Throws(RemoteException::class)
    override fun nodeMissing(addr: Address?) {
        println("NodeMissing was called with $addr")
        if (addr!!.compareTo(myNode!!.neighbours!!.next) == 0) {
            // It's for me
            val myNeighbours = myNode!!.neighbours
            // to my nnext send msg ChPrev with myaddr -> my nnext = next
            myNeighbours!!.next = myNeighbours.nNext
            myNeighbours.nNext = myNode!!.commHub!!.nNext!!.changePrev(myNode!!.address)!!
            // to my prev send msg ChNNext to my.next
            myNode!!.commHub!!.prev!!.changeNNext(myNeighbours.next)
            println("NodeMissing DONE")
        } else {
            // send to the next node
            myNode!!.commHub!!.next!!.nodeMissing(addr)
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
            myNode!!.commHub!!.getRMIProxy(myNode!!.address!!)!!.candidature(id, 0, maxDepth)
            waitUntil { nResp == 2 }
            if (!respOK) {
                myNode!!.state = State.LOST
            }
            maxDepth *= 2
        }
    }

    private fun waitUntil(function: () -> Boolean) {
        while (!function()) {
            Thread.sleep(100)
        }
    }

    @Throws(RemoteException::class)
    override fun candidature(id: Long, minDepth: Int, maxDepth: Int) {
        println("Candidature was called with id $id")
        if (id < myNode!!.nodeId) {
            // Respond to the next and prev
            myNode!!.commHub!!.next!!.response(false, id, myNode!!.address!!)
            myNode!!.commHub!!.prev!!.response(false, id, myNode!!.address!!)
            if (myNode!!.state == State.NOT_INVOLVED) {
                election(myNode!!.nodeId)
            }
        }
        if (id > myNode!!.nodeId) {
            myNode!!.state = State.LOST
            val newMinDepth = minDepth + 1
            println("New minimal depth is $newMinDepth")
            if (newMinDepth < maxDepth) {
                myNode!!.commHub!!.getRMIProxy(myNode!!.address!!)!!.candidature(id, newMinDepth, maxDepth)
            } else {
                // Respond to the next and prev
                myNode!!.commHub!!.next!!.response(true, id, myNode!!.address!!)
                myNode!!.commHub!!.prev!!.response(true, id, myNode!!.address!!)
            }
        }
        if (id == myNode!!.nodeId) {
            if (myNode!!.state == State.ELECTED) {
                myNode!!.state = State.ELECTED
            }

            // I am the winner (myNode.nodeId)
            myNode!!.neighbours!!.leader = myNode!!.address!!
            // Notify the next and prev that I am the winner
            myNode!!.commHub!!.next!!.elected(myNode!!.nodeId)
            myNode!!.commHub!!.prev!!.elected(myNode!!.nodeId)
        }
    }

    @Throws(RemoteException::class)
    override fun response(b: Boolean, id: Long, address: Address) {
        println("Response was called with b $b, id $id and address $address")
        if (id == myNode!!.nodeId) {
            nResp++
            respOK = respOK && b
        } else {
            myNode!!.commHub!!.getRMIProxy(address)!!.response(b, myNode!!.nodeId, address)
        }
    }

    @Throws(RemoteException::class)
    override fun elected(id: Long) {
        println("Elected was called with id $id")
        if (myNode!!.nodeId != id) {
            myNode!!.commHub!!.getRMIProxy(myNode!!.address!!)!!.elected(id)
            myNode!!.nodeId = id
            myNode!!.state = State.NOT_INVOLVED
        }
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

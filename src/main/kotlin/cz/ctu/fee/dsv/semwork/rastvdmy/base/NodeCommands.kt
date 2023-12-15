package cz.ctu.fee.dsv.semwork.rastvdmy.base

import java.rmi.Remote
import java.rmi.RemoteException

interface NodeCommands : Remote {
    @Throws(RemoteException::class)
    fun join(addr: Address?): DSNeighbours?

    //      JOINReply(next, prev, nNext, leader)
    @Throws(RemoteException::class)
    fun changeNNext(addr: Address?)

    @Throws(RemoteException::class)
    fun changePrev(addr: Address?): Address?

    @Throws(RemoteException::class)
    fun nodeMissing(addr: Address?)

    @Throws(RemoteException::class)
    fun election(id: Long)

    @Throws(RemoteException::class)
    fun candidature(id: Long, minDepth: Int, maxDepth: Int)

    @Throws(RemoteException::class)
    fun response(b: Boolean, id: Long, address: Address)

    @Throws(RemoteException::class)
    fun elected(id: Long)

    @Throws(RemoteException::class)
    fun sendMessage(toNickName: String?, fromNickName: String?, message: String?)

    @Throws(RemoteException::class)
    fun register(nickName: String?, addr: Address?)

    @Throws(RemoteException::class)
    fun hello()
}
package cz.ctu.fee.dsv.semwork.rastvdmy.base

import java.io.Serializable

class Address @JvmOverloads constructor(
    @JvmField var hostname: String = "127.0.0.1",
    @JvmField var port: Int = 2010
) : Comparable<Address>, Serializable {
    constructor(addr: Address) : this(addr.hostname, addr.port)

    override fun toString(): String {
        return ("Addr[host:'$hostname', port:'$port']")
    }

    override fun compareTo(other: Address): Int {
        var retVal: Int
        return if ((hostname.compareTo(other.hostname).also { retVal = it }) == 0) {
            if ((port.compareTo(other.port).also { retVal = it }) == 0) {
                0
            } else retVal
        } else retVal
    }
}
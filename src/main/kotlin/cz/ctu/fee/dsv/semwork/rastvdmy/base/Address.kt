package cz.ctu.fee.dsv.semwork.rastvdmy.base

import java.io.Serializable

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

/**
 * This class represents an Address with a hostname and a port.
 * It implements Comparable and Serializable interfaces.
 *
 * @property hostname The hostname of the address, default is "127.0.0.1".
 * @property port The port of the address, default is 2010.
 */
class Address @JvmOverloads constructor(
    @JvmField var hostname: String = "127.0.0.1",
    @JvmField var port: Int = 2010
) : Comparable<Address>, Serializable {

    /**
     * Copy constructor that creates a new Address object with the same hostname and port as the given Address.
     *
     * @param addr The Address object to copy.
     */
    constructor(addr: Address) : this(addr.hostname, addr.port)

    /**
     * Returns a string representation of the Address object.
     *
     * @return A string in the format "Addr[host:'hostname', port:'port']".
     */
    override fun toString(): String {
        return ("Addr[host:'$hostname', port:'$port']")
    }

    /**
     * Compares this Address object with the specified Address object for order.
     * Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     *
     * @param other The Address object to be compared.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    override fun compareTo(other: Address): Int {
        var retVal: Int
        return if ((hostname.compareTo(other.hostname).also { retVal = it }) == 0) {
            if ((port.compareTo(other.port).also { retVal = it }) == 0) {
                0
            } else retVal
        } else retVal
    }
}
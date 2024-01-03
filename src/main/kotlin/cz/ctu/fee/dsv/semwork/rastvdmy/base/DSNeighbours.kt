package cz.ctu.fee.dsv.semwork.rastvdmy.base

import java.io.Serializable

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

/**
 * This class represents a set of neighbours in a distributed system.
 * It implements the Serializable interface to allow it to be sent over the network.
 */
class DSNeighbours : Serializable {

    /**
     * The right neighbour in the distributed system.
     */
    var right: Address = Address()
        set(value) {
            println("right set to $value")
            field = value
        }

    /**
     * The next-next neighbour in the distributed system.
     */
    var nNext: Address = Address()
        set(value) {
            println("nNext set to $value")
            field = value
        }

    /**
     * The left neighbour in the distributed system.
     */
    var left: Address = Address()
        set(value) {
            println("left set to $value")
            field = value
        }

    /**
     * The leader in the distributed system.
     */
    var leader: Address = Address()
        set(value) {
            println("leader set to $value")
            field = value
        }

    /**
     * Constructor that initializes all neighbours to the same address.
     *
     * @param me The address to set all neighbours to.
     */
    constructor(me: Address) {
        this.right = me
        this.nNext = me
        this.left = me
        this.leader = me
    }

    /**
     * Constructor that initializes all neighbours to different addresses.
     *
     * @param right The address of the right neighbour.
     * @param nNext The address of the next-next neighbour.
     * @param left The address of the left neighbour.
     * @param leader The address of the leader.
     */
    constructor(right: Address, nNext: Address, left: Address, leader: Address) {
        this.right = right
        this.nNext = nNext
        this.left = left
        this.leader = leader
    }

    /**
     * Returns a string representation of the DSNeighbours object.
     *
     * @return A string in the format "Neigh[right:'right', nNext:'nNext', left:'left', leader:'leader']".
     */
    override fun toString(): String {
        return ("Neigh[right:'" + right + "', " +
                "nNext:'" + nNext + "', " +
                "left:'" + left + "', " +
                "leader:'" + leader + "']")
    }
}
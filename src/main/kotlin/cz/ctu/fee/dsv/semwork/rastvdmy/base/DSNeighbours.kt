package cz.ctu.fee.dsv.semwork.rastvdmy.base

import java.io.Serializable

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

class DSNeighbours : Serializable {

    @JvmField
    var right: Address
    @JvmField
    var nNext: Address
    @JvmField
    var left: Address
    @JvmField
    var leader: Address


    constructor(me: Address) {
        this.right = me
        this.nNext = me
        this.left = me
        this.leader = me
    }


    constructor(right: Address, nNext: Address, left: Address, leader: Address) {
        this.right = right
        this.nNext = nNext
        this.left = left
        this.leader = leader
    }


    override fun toString(): String {
        return ("Neigh[right:'" + right + "', " +
                "nNext:'" + nNext + "', " +
                "left:'" + left + "', " +
                "leader:'" + leader + "']")
    }
}
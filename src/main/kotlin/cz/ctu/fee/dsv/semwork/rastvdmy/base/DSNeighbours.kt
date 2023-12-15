package cz.ctu.fee.dsv.semwork.rastvdmy.base

import java.io.Serializable

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

class DSNeighbours : Serializable {

    @JvmField
    var next: Address
    @JvmField
    var nNext: Address
    @JvmField
    var prev: Address
    @JvmField
    var leader: Address


    constructor(me: Address) {
        this.next = me
        this.nNext = me
        this.prev = me
        this.leader = me
    }


    constructor(next: Address, nNext: Address, prev: Address, leader: Address) {
        this.next = next
        this.nNext = nNext
        this.prev = prev
        this.leader = leader
    }


    override fun toString(): String {
        return ("Neigh[next:'" + next + "', " +
                "nNext:'" + nNext + "', " +
                "prev:'" + prev + "', " +
                "leader:'" + leader + "']")
    }
}
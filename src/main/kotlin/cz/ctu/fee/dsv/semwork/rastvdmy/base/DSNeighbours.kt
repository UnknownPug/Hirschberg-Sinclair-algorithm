package cz.ctu.fee.dsv.semwork.rastvdmy.base

import java.io.Serializable

/* Source: https://moodle.fel.cvut.cz/pluginfile.php/410384/mod_label/intro/TestSem_v0.1.zip */

class DSNeighbours : Serializable {

    var right: Address
        set(value) {
            println("right set to $value")
            field = value
        }
    var nNext: Address
        set(value) {
            println("nNext set to $value")
            field = value
        }
    var left: Address
        set(value) {
            println("left set to $value")
            field = value
        }
    var leader: Address
        set(value) {
            println("leader set to $value")
            field = value
        }


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
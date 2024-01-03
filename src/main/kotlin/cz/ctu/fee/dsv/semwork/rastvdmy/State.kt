package cz.ctu.fee.dsv.semwork.rastvdmy

/**
 * This enum represents the state of a Node in a distributed system.
 *
 * @property NOT_INVOLVED The Node is not involved in any process.
 * @property CANDIDATE The Node is a candidate in an election process.
 * @property LOST The Node has lost an election.
 * @property ELECTED The Node has won an election and is the leader.
 */
enum class State {
    NOT_INVOLVED,
    CANDIDATE,
    LOST,
    ELECTED
}
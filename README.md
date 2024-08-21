# This program was created as a semester project of the DSVA

## (CTU - SIT winter semester 2024)

### Authors: Dmitry Rastvorov

### Java version: 21

#### ● Project also contains [Dokka - Kotlin documentation](https://unknownpug.github.io/Hirschberg-Sinclair-algorithm/dokka/index.html)
#### ● And also [Documentation in Czech language 🇨🇿](https://github.com/UnknownPug/Hirschberg-Sinclair-algorithm/blob/main/Hirschberg-Sinclair%20Algorithm.pdf)

## Contents

### [Image](#image)

### [Description](#description)

### [How To Run](#howtorun)

-- -- --

### <a name="image"></a> Image
![Hirshberg-Sinclair algorithm image](https://github.com/UnknownPug/Hirschberg-Sinclair-algorithm/assets/73190129/d9fcfccd-3a34-4575-8b05-909eb9fc44ad)

-- -- --

### <a name="description"></a> Description

This project is a simple implementation of the Hirschberg-Sinclair algorithm.
For its implementation was used next technologies and algorithms:

        • Leader Election algorithm
        • Kotlin RMI technology
        • Messaging (chat)

[The Hirschberg-Sinclair algorithm](https://en.wikipedia.org/wiki/Hirschberg–Sinclair_algorithm) is a distributed
algorithm designed for a leader election problem
in a synchronous ring network.

Also, I was inspired by the lectures of lecturer **Ing. Peter
Macejko** [Algorithms for leader selection (symmetry breaking)](https://moodle.fel.cvut.cz/pluginfile.php/410378/mod_label/intro/dsv_pr.07_LE_v2.3_en.pdf)
Where the lecturer explained and demonstrated the essence of the algorithm.

-- -- --

### <a name="howtorun"></a> How To Run

#### 1. Clone the repository

```shell
git clone git@github.com:UnknownPug/Hirschberg-Sinclair-algorithm.git
```

#### 2. Open the project via IDE (for example, [IntelliJ IDEA](https://www.jetbrains.com/idea/))

#### 3. Setting up the workplace

    i. On the right up corner, click on the "Current file".
    ii. Then click on the "Edit Configurations".
    iii. Click on the "Add New" or "+" on the top. Choose the Application

#### 4. Setting up the nodes

Once we have opened the application, we need to configure its settings.
We will set six different nodes, where node 2010 is the leader.
Each node will contain an arguments, which consists of name, ip address, port and ip address and port of the next node.
Also, you must remember that leader node needs to contain only its name ip address and port.
You can also set more nodes:

```
1)
Name: Node 2010
Main class: cz.ctu.fee.dsv.semwork.rastvdmy.Node
Program arguments: Alice 127.0.0.1 2010

2)
Name: Node 2020
Main class: cz.ctu.fee.dsv.semwork.rastvdmy.Node
Program arguments: Bob 127.0.0.1 2020 127.0.0.1 2010

3)
Name: Node 2030
Main class: cz.ctu.fee.dsv.semwork.rastvdmy.Node
Program arguments: Cecil 127.0.0.1 2030 127.0.0.1 2020

4)
Name: Node 2040
Main class: cz.ctu.fee.dsv.semwork.rastvdmy.Node
Program arguments: Dan 127.0.0.1 2040 127.0.0.1 2030

5)
Name: Node 2050
Main class: cz.ctu.fee.dsv.semwork.rastvdmy.Node
Program arguments: Eva 127.0.0.1 2050 127.0.0.1 2040

6)
Name: Node 2060
Main class: cz.ctu.fee.dsv.semwork.rastvdmy.Node
Program arguments: Dima 127.0.0.1 2060 127.0.0.1 2050
```

#### 5. Run the application

After setting up the nodes, we are ready for running the application.
First, we **must** run the leader node, which is node 2010. If we try to run other nodes, we will get an error.
So the sequence of launching nodes should be as follows:

    2010 -> 2020 -> 2030 -> 2040 -> 2050 -> 2060

#### 6. Console commands

After running the application, we can use next console commands:

 ```shell
 h
 ```

It will send "Hello" message to the neighbours, which is left and right nodes.

Example:

Sending node:

    cmd > h
    Sending Hello to both neighbours
    Sending Hello to Proxy[...]
    Sending Hello to Proxy[...]

Receiving node: ```Hello was called ...```


```shell
m
```

It will send a message to the node, which ip address and node will be chosen.

Example:

        cmd > m
        Enter the node address to which you want to send a message:
        127.0.0.1
        Enter the port of the node to which you want to send a message:
        2030
        Enter the message:
        Hello world!

And when we go to the receiving node, we will see the message: ``Message was received: Hello world!``

If we try to send non-existing node (ip address, port) or empty ip address or port, we will get an error message.

Example:

        cmd > m
        Enter the node address to which you want to send a message:
        happydogswimminghomewithabone
        Enter the port of the node to which you want to send a message:
        2070
        Enter the message:
        [empty]
        
        Wrong parameters

```shell
s
```

It will show statistics of the node, from which we called it.

Example:

        cmd > s
        Status: 
        Node[
            id:'2050127000000001', nick:'Eva', myIP:'127.0.0.1', myPort:'2050',
            otherNodeIP:'127.0.0.1', otherNodePort:'2040'] with address Addr[host:'127.0.0.1', port:'2050']
            with neighbours Neigh[right:'Addr[host:'127.0.0.1', port:'2030']',
            nNext:'Addr[host:'127.0.0.1', port:'2040']',
            left:'Addr[host:'127.0.0.1', port:'2040']', 
            leader:'Addr[host:'127.0.0.1', port:'2050']']

```shell
?
```
It will show all available commands.

Example:
        
        cmd > ?
        ? - showing all available commands
        h - send Hello message to both neighbours
        m - send message to a node
        s - print node status

If we try to enter another non-existing command, we will get a message ```Unrecognized command.``` 

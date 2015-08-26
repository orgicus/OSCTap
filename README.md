What is OSCTap
==============

OSCTap is a small utility that makes it simpler to floating point data over the network using the OSC protocol.

This library is in a draft stage at the moment
(read as 'buggy'/rough on the edges)

The library automatically scans ```public float``` variables defined in the sketch:
 * makes them available over OSC as server (sender/host)
 * makes it easy to map local variables with remote variables as a client (receiver/client)

Have a look at this [basic usage demo](https://github.com/orgicus/OSCTap/raw/master/web/OSCTapDemo.mov)

Dependencies
============
OSCTap relies on the oscP5 library.
The [releases](https://github.com/orgicus/OSCTap/releases) page contains version of the library with or without oscP5
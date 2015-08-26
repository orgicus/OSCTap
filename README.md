What is OSCTap
==============

OSCTap is a small utility that makes it simpler to floating point data over the network using the OSC protocol.

The library automatically scans ```public float``` variables defined in the sketch:
 * makes them available over OSC as server (sender/host)
 * makes it easy to map local variables with remote variables as a client (receiver/client)

Usage
=====

Check out the [basic usage demo](https://github.com/orgicus/OSCTap/raw/master/web/OSCTapDemo.mov)

Additionally see the Sender/Receiver.

Minimal Sender, continuosly sending a value:

```java
import oscP5.*;
import tap.osc.OSCSender;

public float x = 0;
OSCSender s;

void setup(){
  size(400,400);
  s = new OSCSender(this);
}
void draw(){
  x = constrain((float)mouseX/width,0.0,1.0);
  s.send(true);
  background(x * 255);
}
```
Receiver:
```
import oscP5.*;
import tap.osc.OSCReceiver;

public float y = 0;
OSCReceiver r;

void setup(){
  size(400,400);
  r = new OSCReceiver(this);
}
void draw(){
  background(255);
  ellipse(width * .5, y * height, 10,10);
}
```

How does it work
================

Initial messaging works by broadcasting across all IPs.
No need to worry about IPs at this stage, but this isn't efficient
so it's only used when searching for hosts/sender instances accross the network.

Currently the focus is simply sending a float variable.
Each message however contains three arguments, the 2nd argument being the actual value.

Based on [Alois Yang](http://aloisyang.com)'s suggestion during the [Gen-AV hackathon](http://gen-av.org), 
we explored the idea of sharing data from Processing to other audio software 
using [OSCulator to bridge OSC to MIDI](http://cote.cc/blog/using-osculator-as-an-osc-to-midi-bridge)

I realise the limitation of float values, but for now let's imagine as a creative constraint :)
A bit of R&D into OSC/MIDI in between is next.

Dependencies
============
OSCTap relies on the oscP5 library.
The [releases](https://github.com/orgicus/OSCTap/releases) page contains version of the library with or without oscP5
If you already have oscP5, use the release without oscP5 and be sure import oscP5 in sketches using OSCTap.

Caveats
=======
This library is in a draft stage at the moment
(read as 'buggy'/rough on the edges)
Documentation and more examples will follow.


import tap.osc.*;
//declare a sender object
OSCSender sender;

//declare public float variables to be shared
public float x = 1.0;
public float y = 1.0;
public float velocity = 1.0;
public float amplitude = 1.0;

void setup() {
  size(400,400);
  //initialize the sender
  sender = new OSCSender(this);
  //set a prefix (not compulsory, but a good idea, especially if using multiple sketching sending on the same network)
  sender.setPrefix("/input/tap");
}
void draw() {
  background(127);
  ellipse(x * width,y * width, amplitude * 100, amplitude * 100);
}
void mouseDragged(){
  //update variables
  x = constrain((float)mouseX/width,0.0,1.0);
  y = constrain((float)mouseY/height,0.0,1.0);
  velocity = random(1.0);
  amplitude = random(1.0);
  //send variables as soon as they're updated
  //the true means send a 1 (ON), handy when bridging OSC to MIDI
  sender.send(true);
}

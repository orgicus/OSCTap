import tap.osc.*;
//declare a receiver object
OSCReceiver receiver;

public float a = 1.0f;
public float b = 1.0f;
public float c = 1.0f;
public float d = 1.0f;

float cx,cy,qw,qh;


void setup() {
  size(400,400);
  //initialize the receiver
  //this allows you to click the tap (t) icon to: 
  //  1.search for available public float variables
  //  2. connect float variables from the network to float variables in the current sketch (by dragging and dropping from one to the other)  
  receiver = new OSCReceiver(this);
    
  cx = width * .5;
  cy = height * .5;
  qw = cx * .5;
  qh = cy * .5;
}

void draw() {
  background(127);
  quad(cx - (qw * a),cy - (qh * a),
       cx + (qw * b),cy - (qh * b),
       cx + (qw * c),cy + (qh * c),
       cx - (qw * d),cy + (qh * d));
}

package tap.ui;

import processing.core.PApplet;

public class TapButton extends ToggleButton{
	
	public TapButton(PApplet parent, String label, float x, float y, float w, float h) {
		super(parent, label, x, y, 32, 32);
	}
	public void draw(){
		parent.pushStyle();
		parent.noStroke();
	    parent.fill(isOn ? fg : bg);
	    parent.rect(x,y,w,h);
	    parent.stroke(isOn ? bg : fg);
	    parent.pushMatrix();
	    parent.translate(x+w*.25f,y+h*.125f);
	    parent.line(4,2,8,2);
	    parent.line(8,2,8,7);
	    parent.line(8,7,11,7);
	    parent.line(11,7,11,10);
	    parent.line(11,10,8,10);
	    parent.line(8,10,8,17);
	    parent.bezier(8,17,8,17,7,19,11,17);
	    parent.line(11,17,12,21);
	    parent.bezier(12,21,12,21,11,22,8,22);
	    parent.bezier(8,22,5,22,4,20,4,19);
	    parent.bezier(4,19,4,17,4,11,4,11);
	    parent.line(4,11,2,11);
	    parent.line(2,11,2,7);
	    parent.line(2,7,4,7);
	    parent.line(4,7,4,2);
	    parent.popMatrix();
	    parent.popStyle();
	}
}
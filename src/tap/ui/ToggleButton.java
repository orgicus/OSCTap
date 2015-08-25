package tap.ui;

import processing.core.PApplet;
import processing.event.MouseEvent;

public class ToggleButton extends Button{
	public boolean isOn = false;
	
	ToggleButton(PApplet parent, String label, float x, float y, float w,float h) {
		super(parent, label, x, y, w, h);
	}
	public void update(MouseEvent e){
		super.update(e);
		if(e.getAction() == MouseEvent.CLICK && isOver) isOn = !isOn;
	}
	public void draw(){
		parent.pushStyle();
	    parent.noStroke();
	    parent.fill(isOn ? fg : bg);
	    parent.rect(x,y,w,h);
	    parent.fill(isOn ? bg : fg);
	    parent.text(label,x+pw,y+h*.75f);
	    parent.popStyle();
	}
}
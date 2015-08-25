package tap.ui;

import processing.core.PApplet;
import processing.event.MouseEvent;

public class Button extends GUIElement{
	
	boolean isOver,wasPressed;
	int pw = 10;
	
	//TODO handle width resize based on label
	public Button(PApplet parent, String label, float x, float y, float w, float h) {
		super(parent, label, x, y, w, h);
		float tw = parent.textWidth(label);
		if(tw > this.w) this.w = tw;
	}
	public void update(MouseEvent e){
		if(!enabled) return;
		isOver = ((parent.mouseX >= this.x && parent.mouseX <= (this.x+this.w))&&
				  (parent.mouseY >= this.y && parent.mouseY <= (this.y+this.h)));
		switch(e.getAction()){
		  	case MouseEvent.CLICK:
		  		if(isOver && listener != null) listener.onGUIEvent(this, e);
			break;
		  	case MouseEvent.PRESS:
		  		if(isOver && listener != null) listener.onGUIEvent(this, e);
		  		break;
		  	case MouseEvent.RELEASE:
		  		if(isOver && listener != null) listener.onGUIEvent(this, e);
		  		break;
		  	case MouseEvent.MOVE:
		  		break;
	  }
	}
	public void draw(){
		parent.pushStyle();
	    parent.noStroke();
	    parent.fill(isOver ? fg : bg);
	    parent.rect(x,y,w,h);
	    parent.fill(isOver ? bg : fg);
	    parent.text(label,x+pw,y+h*.75f);
	    parent.popStyle();
	}
}

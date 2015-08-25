package tap.ui;

import processing.core.PApplet;
import processing.event.MouseEvent;

public class GUIElement{
	
	public boolean enabled = true;
	
	public float w,h,x,y;//width, height and position
	public int bg = 0xFFC8C8C8;//background colour
	public int fg = 0xFF000000;//foreground colour
	public String label;
	
	PApplet parent;
	
	GUIListener listener;
	
	public GUIElement(PApplet parent,String label,float x,float y,float w,float h){
		this.x = x;
	    this.y = y;
	    this.w = w;
	    this.h = h;
	    this.label = label;
	    this.parent = parent;
	}
	public void update(MouseEvent e){}
	public void draw(){}
	public void setListener(GUIListener l){
		listener = l;
	}
}
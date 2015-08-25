package tap.ui;

import java.util.ArrayList;

import processing.event.MouseEvent;

public class GUIGroup{
	public float x;
	public float y;
	public float pad = 5;
	
	ArrayList<GUIElement> elements = new ArrayList<GUIElement>();
	
	
	public void halign(){
		for (int i = 1; i < elements.size(); i++) {
			GUIElement c = elements.get(i);
			GUIElement p = elements.get(i-1);
			c.x = p.x + p.w + pad;
		}
	}
	public void valign(){
		for (int i = 1; i < elements.size(); i++) {
			GUIElement c = elements.get(i);
			GUIElement p = elements.get(i-1);
			c.y = p.y + p.h + pad;
		}
	}
	public void update(MouseEvent e){
		try{
			for(GUIElement g : elements) g.update(e);
		}catch(java.util.ConcurrentModificationException ex){
			
		}
	}
	public void draw(){
		try{
			for(GUIElement g : elements) g.draw();
		}catch(java.util.ConcurrentModificationException ex){
			
		}
	}
	public void add(GUIElement g){
		g.x = this.x;
		g.y = this.y;
		elements.add(g);
	}
	public void remove(GUIElement g){
		if(elements.contains(g)) elements.add(g);
	}
	public boolean contains(GUIElement g){
		return elements.contains(g);
	}
	public GUIElement getElementByLabel(String label){
		GUIElement e = null;
		for(GUIElement g : elements)
			if (g.label.contains(label)) return g;
		return e;	
	}
}
package tap.osc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.JSONObject;
import processing.data.StringDict;
import processing.event.MouseEvent;
import tap.ui.Button;
import tap.ui.GUIElement;
import tap.ui.GUIGroup;
import tap.ui.GUIListener;
import tap.ui.TapButton;

public class OSCReceiver implements GUIListener {
	
	private static final int PORT_IN = 12000;
	private static final int PORT_OUT = 12001;
  
  
	private final NetAddress broadcast;
	private final String TAG_SEARCH = "/search";
	private final String TAG_RESULT = "/result";
	private final String TAG_SUBSCRIBE = "/subscribe";
	private final String TAG_UNSUBSCRIBE = "/unsubscribe";
	
	private final OscMessage SEARCH = new OscMessage(TAG_SEARCH,new Object[]{PORT_IN});
  
	private OscP5 osc;
	private PApplet parent;
	
	//UI
	private boolean visible = true;
	private float buttonWidth = 150;
	private float buttonHeight= 20;
	private TapButton tap;
	private Button search;
	private GUIGroup localControls = new GUIGroup(); 
	private GUIGroup remoteControls = new GUIGroup(); 
	private GUIElement lastPress,lastRelease;
	
	  //updates
	private HashMap<String,Parameter> localParams = new HashMap<String,Parameter>();
	private HashMap<String,Parameter> remoteParams = new HashMap<String,Parameter>();
	
	private StringDict mapping = new StringDict();
	
	private JSONObject session = new JSONObject();
	
	public OSCReceiver(PApplet parent){
		this.parent = parent;
	    osc = new OscP5(this,PORT_OUT);
//	    broadcast = new NetAddress("255.255.255.255",type == OSCNode.INPUT ? PORT_OUT : PORT_IN);
	    broadcast = new NetAddress("255.255.255.255",PORT_IN);
//	    broadcast = new NetAddress("127.0.0.1",PORT_IN);
//	    PApplet.println(broadcast);
	    parent.registerMethod("draw", this);
	    parent.registerMethod("dispose",this);
	    parent.registerMethod("mouseEvent", this);
	    
	    localControls.x = 10;
	    remoteControls.x = parent.width - buttonWidth - remoteControls.pad;
	    localControls.y = remoteControls.y = 75;
	    
	    try{
	      for (Field f : parent.getClass().getDeclaredFields()) {
	        if(f.getModifiers() == Modifier.PUBLIC) {
	          
	          Parameter p = new Parameter();
	          p.name = f.getName();
	          p.field = f;
	          
	          Button b = new Button(parent, p.name, 0, 0, buttonWidth, buttonHeight);
	          b.setListener(this);
	          p.gui = b;
	          localControls.add(b);
	          localControls.valign();
	          
	          p.connectorPos = new PVector(b.x + b.w,b.y + (b.h * .5f));
//	          PApplet.println(p.name+".connectorPos = " + p.connectorPos);
	          localParams.put(p.name, p);
	        }
	      }
	    }catch(Exception e){
	      e.printStackTrace();
	    }
	    
	    tap = new TapButton(parent, "", 10, 10, 32, 32);
	    
	    search = new Button(parent, "search", 10, 45, buttonWidth, buttonHeight);
	    search.setListener(this);
	    	
	}
	 public void mouseEvent(MouseEvent e){
		  tap.update(e);
		  if(tap.isOn){
			  localControls.update(e);
			  remoteControls.update(e);
			  search.update(e);
		  }
		  if(e.getAction() == MouseEvent.RELEASE && lastPress != null){
//			  PApplet.println(lastPress.label+" is in localControls:"+localControls.contains(lastPress));
			  lastPress = null;
		  }
	  }
	 
	  //TODO shift + click to remove connection
	  public void onGUIEvent(GUIElement g,MouseEvent e){
		  int type = e.getAction();
//		  PApplet.println(g.label + ":"+type);
		  if(g.equals(search)){
			  if(type == MouseEvent.CLICK) search();
		  }else{
			  boolean isRemote = remoteControls.contains(g);
			  if(type == MouseEvent.PRESS){
				  if(lastPress == null) lastPress = g;
			  }
			  if(type == MouseEvent.RELEASE){
//				  if(lastRelease== null) lastRelease = e;
				  
				  if(e.isShiftDown()){
					  
					  if(isRemote){
//						  PApplet.println("connecting " + lastPress.label + " to " + g.label);
						  unmap(g.label,lastPress.label);
					  }else{
//						  PApplet.println("connecting " + g.label + " to " + lastPress.label);
						  unmap(lastPress.label,g.label);
					  }
					  
				  }else{
				  
					  if(lastPress != null && lastPress != g){
						  if(isRemote){
//							  PApplet.println("connecting " + lastPress.label + " to " + g.label);
							  map(g.label,lastPress.label);
						  }else{
//							  PApplet.println("connecting " + g.label + " to " + lastPress.label);
							  map(lastPress.label,g.label);
						  }
					  }
				  }
				  
//				  if(lastPress != null && remoteControls.contains(e)){
//					  connectionUI.put(new PVector(lastPress.x+lastPress.w, lastPress.y+(lastPress.h * .5f)), new PVector(e.x, e.y + (e.h * .5f)));
//					  map(e.label,lastPress.label);
//					  parent.println("connecting " + lastPress.label + " to " + e.label);
//				  }
				  
			  }
		  }
	  }
	  void map(String remoteParameter,String localParameter){
		  
		  String mappedValue = mapping.get(localParameter);
		  
//		  System.out.println("mappedValue: " + mappedValue);
		  PApplet.println(mapping);
		  
		  
		  if(mappedValue != null){//unsubscribe if previously subscribed
//			  System.out.println("swapping " + mappedValue + " for " + remoteParameter);
			  Parameter oldParam = remoteParams.get(mappedValue);
			  osc.send(new OscMessage(TAG_UNSUBSCRIBE,new Object[]{remoteParameter}),oldParam.remoteAddress);
		  }
		  if(remoteParams.get(remoteParameter) != null){
			  Parameter remote = remoteParams.get(remoteParameter);
			  osc.send(new OscMessage(TAG_SUBSCRIBE,new Object[]{remoteParameter}),remote.remoteAddress);
			  mapping.set(remoteParameter,localParameter);
		  }
		  mapping.set(localParameter,remoteParameter);
	  }
	  
	  void unmap(String remoteParameter,String localParameter){
		  
		  String mappedValue = mapping.get(localParameter);
		  
//		  System.out.println("mappedValue: " + mappedValue);
//		  PApplet.println(mapping);
		  
		  if(mappedValue != null){//unsubscribe if previously subscribed
			  Parameter oldParam = remoteParams.get(mappedValue);
			  osc.send(new OscMessage(TAG_UNSUBSCRIBE,new Object[]{remoteParameter}),oldParam.remoteAddress);
			  mapping.remove(localParameter);
		  }
	  }
	  
	  void oscEvent(OscMessage m){
//		    m.print();
		//handle search result
	    if(m.checkAddrPattern(TAG_RESULT)){
	      int argsLen = m.arguments().length;
	      NetAddress to = OSCUtils.getAddressFromBroadcast(m,PORT_IN);
	      
	      for(int i = 0 ; i < argsLen; i++){
//	        PApplet.println("read param:" + m.get(i));
	    	  String remoteParam = m.get(i).stringValue();
	    	  
	    	  if(remoteParams.get(remoteParam) == null){
	        
		        Parameter p = new Parameter();
		        p.name = remoteParam;
		        p.remoteAddress = to;
		        Button btn = new Button(parent, p.name, 0, 0, buttonWidth, buttonHeight);
		        btn.setListener(this);
		        remoteControls.add(btn);
		        remoteControls.valign();
		        p.gui = btn;
		        
		        p.connectorPos = new PVector(btn.x,btn.y + (btn.h * .5f));
//		        PApplet.println(p.name+".connectorPos = " + p.connectorPos);
		        
		        remoteParams.put(p.name, p);
	        
	    	  }
	      }
	      
	    }
		//handle subscribed fields   
	    for(String localParamName : mapping.keyArray()){
	    	String remoteParamName = mapping.get(localParamName);
	    	if(m.checkAddrPattern(remoteParamName)){
	    		float value = m.get(1).floatValue();
	    		
	    		Field f = localParams.get(localParamName).field;
	    		if(f != null){
	    			try{
	    				f.setFloat(parent, value);
	    			}catch(Exception e){//Pokemon exception handling
	    				e.printStackTrace(System.err);
	    			}
	    		}
	    		
	    	}
	    }
	  }
	  public void draw(){
		  if(!visible ) return;
		  tap.draw();
		  if(tap.isOn){
			  localControls.draw();
			  remoteControls.draw();
			  search.draw();
			  if(lastPress != null){
				  parent.pushStyle();
				  parent.line(lastPress.x + (localControls.contains(lastPress) ? lastPress.w : 0), lastPress.y+(lastPress.h * .5f), parent.mouseX, parent.mouseY);
				  parent.popStyle();
			  }
			  drawConnections();
		  }
	  }
	private void drawConnections() {
		// TODO Auto-generated method stub
		parent.beginShape(PApplet.LINES);
		for(String localParamName : mapping.keyArray()){
			String remoteParamName = mapping.get(localParamName);
			Parameter local = localParams.get(localParamName);
			Parameter remote = remoteParams.get(remoteParamName);
			
			if(local != null && remote != null){
				PVector from = localParams.get(localParamName).connectorPos;
				PVector to   = remoteParams.get(remoteParamName).connectorPos;
				if(from != null && to != null){
					parent.vertex(from.x,from.y);
					parent.vertex(to.x,to.y);	
				}
			}
		}
		parent.endShape();
	}
	public void search(){
		osc.send(SEARCH,broadcast);
	}
	public void show(){
		visible = true;
	}
	public void hide(){
		visible = false;
	}
	public void toggle(){
		visible = !visible;
	}
	public void dispose(){
//		session.remove("mapping");//clear any previous data
		
		osc.dispose();
	}
	
	class Parameter{
		
		String name;
		String remoteParameter;//duplicated field
//		String remoteAddress;
		NetAddress remoteAddress;
		Field field;
		GUIElement gui;
		PVector connectorPos;
		
		Parameter(){
			
		}
		
	}
}


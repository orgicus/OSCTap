package tap.osc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import netP5.NetAddress;
/*
discover -> returns a list of available servers, each list which a list of properties to subscribe to
ui -> displays a list of public variables plus a list of properties to subscribe to
   -> on connection made -> subscribe to server(s)
                         -> save settings to json file
   -> use icon to toggle ui
   -> (for now) use shift+click connection to disconnect
-> for subscribe and discover repeat until confirmed

settings:
{
  "port in":12000,
  "port out":12001,
  "map":
  {
    "a":"x",
    "b":"y",
    "c":"vel",
    "d":"amp"
  }
}
*/
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.event.MouseEvent;
import tap.ui.Button;
import tap.ui.GUIElement;
import tap.ui.GUIGroup;
import tap.ui.GUIListener;
import tap.ui.TapButton;


public class OSCNode implements GUIListener{
  
  private static final int PORT_IN = 12000;
  private static final int PORT_OUT = 12001;
  
  
  private final NetAddress broadcast;
  private final String TAG_SEARCH = "/search";
  private final String TAG_RESULT = "/result";
  private final String TAG_SUBSCRIBE = "/subscribe";
  private final String TAG_UNSUBSCRIBE = "/unsubscribe";
  private final OscMessage SEARCH = new OscMessage(TAG_SEARCH,new Object[]{PORT_IN});
  
  public final static int INPUT = 0;
  public final static int OUTPUT = 1;
  private int type;
  
  private OscP5 osc;
  
  
//  private HashMap<String,Field> parameterMap = new HashMap<String,Field>();
//
//  private ArrayList<RemoteSource> sources = new ArrayList<RemoteSource>();
//  private HashMap<String,NetAddress> remoteParameters = new HashMap<String,NetAddress>();
  
  private HashMap<String,String> mapping = new HashMap<String,String>();
  
  private HashMap<String,NetAddress> remotesLocations = new HashMap<String,NetAddress>(); 
  
  private PApplet parent;
  private boolean autoMode = false;
  private String prefix = "";
  
  //UI
  private boolean visible = true;
  private float buttonWidth = 150;
  private float buttonHeight= 20;
  private TapButton tap;
  private Button search;
  private GUIGroup localControls = new GUIGroup(); 
  private GUIGroup remoteControls = new GUIGroup(); 
  private GUIElement lastPress,lastRelease;
  private HashMap<PVector,PVector> connectionUI = new HashMap<PVector,PVector>();  
  
  //updates
  private HashMap<String,Parameter> localParams = new HashMap<String,Parameter>();
  private HashMap<String,Parameter> remoteParams = new HashMap<String,Parameter>();
  
  OSCNode(PApplet parent,int type){
  
    this.parent = parent;
    this.type = type;
    osc = new OscP5(this,type == OSCNode.INPUT ? PORT_IN : PORT_OUT);
//    broadcast = new NetAddress("255.255.255.255",type == OSCNode.INPUT ? PORT_OUT : PORT_IN);
    broadcast = new NetAddress("127.0.0.1",type == OSCNode.INPUT ? PORT_OUT : PORT_IN);
    PApplet.println(broadcast);
    parent.registerMethod("draw", this);
    parent.registerMethod("dispose",this);
    parent.registerMethod("mouseEvent", this);
    
    try{
      for (Field f : parent.getClass().getDeclaredFields()) {
        if(f.getModifiers() == Modifier.PUBLIC) {
//          parameterMap.put(f.getName(),f);
          
          Parameter p = new Parameter();
          p.name = f.getName();
          p.field = f;
          
          Button b = new Button(parent, p.name, 0, 0, buttonWidth, buttonHeight);
          b.setListener(this);
          p.gui = b;
          localControls.add(b);
          
          p.connectorPos = new PVector(b.x + b.w,b.y + (b.h * .5f));
          
          localParams.put(p.name, p);
        }
      }
      localControls.valign();
    }catch(Exception e){
      e.printStackTrace();
    }
    
    tap = new TapButton(parent, "", 10, 10, 32, 32);
//    tap.setListener(this);
    localControls.x = 10;
    remoteControls.x = parent.width - buttonWidth - remoteControls.pad;
    localControls.y = remoteControls.y = 75;
    if(type == OSCNode.OUTPUT){
    	search = new Button(parent, "search", 10, 45, buttonWidth, buttonHeight);
    	search.setListener(this);
    	
//    	for(String localParam : parameterMap.keySet()) {
//    		Button b = new Button(parent, localParam, 0, 0, buttonWidth, buttonHeight);
//    		b.setListener(this);
//    		localControls.add(b);
//    	}
//    	localControls.valign();
    }
  }
  
  public void mouseEvent(MouseEvent e){
	  tap.update(e);
	  if(tap.isOn){
		  localControls.update(e);
		  remoteControls.update(e);
		  if(type == OSCNode.OUTPUT) search.update(e);
	  }
	  if(e.getAction() == MouseEvent.RELEASE && lastPress != null){
		  PApplet.println(lastPress.label+" is in localControls:"+localControls.contains(lastPress));
		  lastPress = null;
	  }
  }

  //TODO handle press/release from local or remote controls -> allow connections from right to left
  //TODO check if connection exists
  //TODO shift + click to remove connection
  public void onGUIEvent(GUIElement g,MouseEvent e){
	  int type = e.getAction();
	  PApplet.println(g.label + ":"+type);
	  if(g.equals(search)){
		  if(type == MouseEvent.CLICK) search();
	  }else{
		  boolean isRemote = remoteControls.contains(g);
		  if(type == MouseEvent.PRESS){
			  if(lastPress == null) lastPress = g;
		  }
		  if(type == MouseEvent.RELEASE){
//			  if(lastRelease== null) lastRelease = e;
			  
			  if(lastPress != null && lastPress != g){
				  if(isRemote){
					  connectionUI.put(new PVector(lastPress.x+lastPress.w, lastPress.y+(lastPress.h * .5f)), new PVector(g.x, g.y + (g.h * .5f)));
					  map(g.label,lastPress.label);
					  PApplet.println("connecting " + lastPress.label + " to " + g.label);
				  }else{
					  connectionUI.put(new PVector(lastPress.x, lastPress.y+(lastPress.h * .5f)), new PVector(g.x+g.w, g.y + (g.h * .5f)));
					  map(lastPress.label,g.label);
					  PApplet.println("connecting " + g.label + " to " + lastPress.label);
				  }
			  }
			  
//			  if(lastPress != null && remoteControls.contains(e)){
//				  connectionUI.put(new PVector(lastPress.x+lastPress.w, lastPress.y+(lastPress.h * .5f)), new PVector(e.x, e.y + (e.h * .5f)));
//				  map(e.label,lastPress.label);
//				  parent.println("connecting " + lastPress.label + " to " + e.label);
//			  }
			  
		  }
	  }
  }
  
  void draw(){
	  if(autoMode) send(true);
	  if(!visible ) return;
	  tap.draw();
	  if(tap.isOn){
		  localControls.draw();
		  remoteControls.draw();
		  if(type == OSCNode.OUTPUT) search.draw();
		  if(lastPress != null){
			  parent.pushStyle();
			  parent.line(lastPress.x + (localControls.contains(lastPress) ? lastPress.w : 0), lastPress.y+(lastPress.h * .5f), parent.mouseX, parent.mouseY);
			  parent.popStyle();
		  }
		  drawConnections();
	  }
  }
  private void drawConnections() {
	  parent.pushStyle();
	  parent.beginShape(PConstants.LINES);
	  for (PVector from : connectionUI.keySet()){
		  PVector to = connectionUI.get(from);
		  parent.vertex(from.x, from.y);
		  parent.vertex(to.x, to.y);
	  }
	  parent.endShape();
	  parent.popStyle();
  }

  void search(){
    osc.send(SEARCH,broadcast);
  }
  
  void map(String remoteParameter,String localParameter){
	  if(remoteParams.get(remoteParameter) != null){
		  Parameter remote = remoteParams.get(remoteParameter);
		  NetAddress to = new NetAddress(remote.remoteAddress,PORT_OUT);
		  osc.send(new OscMessage(TAG_SUBSCRIBE,new Object[]{remoteParameter}),to);
		  mapping.put(remoteParameter,localParameter);
	  }
    /*
	 if(remoteParameters.get(remoteParameter) != null){
      NetAddress to = remoteParameters.get(remoteParameter);
      osc.send(new OscMessage(TAG_SUBSCRIBE,new Object[]{remoteParameter}),to);
      mapping.put(remoteParameter,localParameter);
    }
    */
//      osc.send(new OscMessage(TAG_SUBSCRIBE,
  }
  void unmap(String remoteParameter,String localParameter){
	  if(remoteParams.get(remoteParameter) != null){
		  Parameter remote = remoteParams.get(remoteParameter);
		  NetAddress to = new NetAddress(remote.remoteAddress,PORT_OUT);
		  osc.send(new OscMessage(TAG_UNSUBSCRIBE,new Object[]{remoteParameter}),to);
		  mapping.remove(remoteParameter);
	  }
	  /*
	  if(remoteParameters.get(remoteParameter) != null){
	      NetAddress to = remoteParameters.get(remoteParameter);
	      osc.send(new OscMessage(TAG_UNSUBSCRIBE,new Object[]{remoteParameter}),to);
//	      mapping.put(remoteParameter,localParameter);
	      mapping.remove(remoteParameter);
	    }
	    */ 
  }

  void send(boolean on){
	  for (String paramName : localParams.keySet()){
		  Parameter localParam;
	  }
	  /*
	PApplet.println("sending, sources:"+sources.size());
    for(RemoteSource src : sources){
      for(String lparam: src.parameters){
        String param = lparam.substring(prefix.length()+1);
        PApplet.println(param);
        if(parameterMap.get(param) != null){
          try{
            float value = parameterMap.get(param).getFloat(parent);
            PApplet.println(value+":"+src.netAddress);
            osc.send(new OscMessage(lparam,new Object[]{0,value,on ? 1 : 0}),src.netAddress);
          }catch(Exception e){
            e.printStackTrace();
          }
        }
      }
    }
    //*/
  }
  
  NetAddress getAddressFromBroadcast(OscMessage m){
	  String ip = m.address().substring(1);//address starts with a / so we discard it
      int port  = broadcast.port();
      return new NetAddress(ip,port);
  }
  
  void oscEvent(OscMessage m){
    System.err.println("OSCNodeReceiveTest");
//    m.print();
//    out += "received: " + m + "\n";
    if(m.checkAddrPattern(TAG_RESULT)){
      int argsLen = m.arguments().length;
      //FIXME check for duplicates first
      NetAddress to = getAddressFromBroadcast(m);
      RemoteSource src = new RemoteSource(to);
      
      for(int i = 0 ; i < argsLen; i++){
//        println("read param:" + m.get(i));
    	  String remoteParam = m.get(i).stringValue();
        src.parameters.add(remoteParam);
        remotesLocations.put(remoteParam,to);
        //remoteParameters.put(remoteParam,to);
//        Button b = new Button(parent, remoteParam, 0, 0, buttonWidth, buttonHeight);
//        b.setListener(this);
//        remoteControls.add(b);
        
        Parameter p = new Parameter();
        p.name = remoteParam;
        
        Button btn = new Button(parent, p.name, 0, 0, buttonWidth, buttonHeight);
        btn.setListener(this);
        p.gui = btn;
        
        p.connectorPos = new PVector(btn.x,btn.y + (btn.h * .5f));
        
        remoteParams.put(p.name, p);
        
      }
      remoteControls.valign();
      
      
//      sources.add(src);
    }

     if(m.checkAddrPattern(TAG_SEARCH)){
        NetAddress from = m.netAddress();
        OscMessage result = new OscMessage(TAG_RESULT);
//        for(String s: parameterMap.keySet()) result.add(prefix+"/"+s);
        for(String s: localParams.keySet()) result.add(prefix+"/"+s);
        osc.send(result,getAddressFromBroadcast(m));
        PApplet.println(from + " is searching");
        PApplet.println(result);
//        out += from + " is searching\n";
//        out += "result " + result;
      }
      if(m.checkAddrPattern(TAG_SUBSCRIBE)){
        String field = m.get(0).stringValue();
        PApplet.println("request to subscribe to " + field);
        
        //TODO check if exists first
        String ip = m.address().substring(1);//address starts with a / so we discard it
        
        //check if ip was already subscribed
        RemoteSource existing = null;
        //TODO implement
        /*
        if(sources.size() > 0){
        	for(RemoteSource s : sources){
        		if(s.netAddress.address().contains(ip)){
        			PApplet.println("client " + ip + " already subscribed");
        			existing = s;
        			break;
        		}
        	}
        }
        
        if(existing == null){
	        RemoteSource src = new RemoteSource(new NetAddress(ip,broadcast.port()));
	        src.parameters.add(field);
	        
	        sources.add(src);
	        PApplet.println(sources+":"+src.parameters);
        }else{
        	if(existing.parameters.indexOf(field) == -1){
        		existing.parameters.add(field);
        		PApplet.println("added" + field + " to " + ip);
        	}
        }
        //*/
      }
     
      
      for(String remoteParamName : remoteParams.keySet()){
    	  Parameter remoteParam = remoteParams.get(remoteParamName);
    	  if(m.checkAddrPattern(remoteParamName)){
    		  float value = m.get(1).floatValue();
    		  String localParamName = mapping.get(remoteParamName);
    		  if(localParamName != null){
    			  Field f = localParams.get(localParamName).field;
    			  try{
    				  f.setFloat(parent, value);
    			  }catch (Exception e){
    				  e.printStackTrace();
    			  }
    		  }
    	  }
      }
    /* 
    Set<String> watchList = mapping.keySet();
    for(String param : watchList){
      if(m.checkAddrPattern(param)){
        float value = m.get(1).floatValue();
        Field f = parameterMap.get(mapping.get(param));
        if(f != null){
          try{
            f.setFloat(parent,value);
          }catch (Exception e){
            e.printStackTrace();
          }  
        }
      } 
    }
    //*/
    
//      println(m.netAddress());
  }
  
  void show(){
	  visible = true;
  }
  void hide(){
	  visible = false;
  }
  void toggle(){
	  visible = !visible;
  }
  
  void dispose(){
	  PApplet.println("cleanup");
  }
  
}
class RemoteSource{
//  
  NetAddress netAddress;
  ArrayList<String> parameters = new ArrayList<String>();
  
  RemoteSource(NetAddress netAddress){
    this.netAddress = netAddress;
  }
  
}
/*
 * parameter (local or remote) -> maybe swap left to right ?
 * parameter -> name -> string, function, guiElement, connectionPos 
 * local map: HashMap<String,Parameter>
 * remote map: HashMap<String,Parameter>
 * mapping: HashMap<String,String>
 */

class Parameter{
	
	String name;
	String remoteParameter;//duplicated field
	String remoteAddress;
//	NetAddress remoteAddress;
	Field field;
	GUIElement gui;
	PVector connectorPos;
	
	Parameter(){
		
	}
	
}
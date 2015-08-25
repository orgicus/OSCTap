package tap.osc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

public class OSCSender {
	
	private static final int PORT_IN = 12000;
	private static final int PORT_OUT = 12001;
  
  
	private final NetAddress broadcast;
	private final String TAG_SEARCH = "/search";
	private final String TAG_RESULT = "/result";
	private final String TAG_SUBSCRIBE = "/subscribe";
	private final String TAG_UNSUBSCRIBE = "/unsubscribe";
  
	private OscP5 osc;
	
	private PApplet parent;
	private String prefix = "";
	private HashMap<String,Parameter> localParams = new HashMap<String,Parameter>();
	private HashMap<NetAddress,ArrayList<String>> subscribers = new HashMap<NetAddress,ArrayList<String>>();
	private HashMap<String,ArrayList<NetAddress>> subscribedFields = new HashMap<String,ArrayList<NetAddress>>();
	//TODO figure out correct data structure...double map or two hashmaps
	private boolean autoMode;
	
	public OSCSender(PApplet parent){
		  
	    this.parent = parent;
	    osc = new OscP5(this,PORT_IN);
//	    broadcast = new NetAddress("127.0.0.1",PORT_OUT);
	    broadcast = new NetAddress("255.255.255.255",PORT_OUT);
	    
	    parent.registerMethod("draw", this);
	    parent.registerMethod("dispose",this);
//	    parent.registerMethod("mouseEvent", this);
	    
	    try{
	        for (Field f : parent.getClass().getDeclaredFields()) {
	          if(f.getModifiers() == Modifier.PUBLIC) {
	            
	            Parameter p = new Parameter();
	            p.name = f.getName();
	            p.field = f;
	            
	            localParams.put(p.name, p);
	          }
	        }
	      }catch(Exception e){
	        e.printStackTrace();
	      }
	}
	
	public void setPrefix(String prefix){
		this.prefix = prefix;
		if(this.prefix == null) this.prefix = "";
	}
	
	public void draw(){
	  if(autoMode) send(true);
	}
	
	void printHashMap(HashMap h){
		String out = h.keySet().size()+" elements\n";
		for(Object key  : h.keySet()) out += key+"="+h.get(key)+"\n";
//		PApplet.println(out);
	}
	void oscEvent(OscMessage m){
		NetAddress netAddress = OSCUtils.getAddressFromBroadcast(m,PORT_OUT);
	     if(m.checkAddrPattern(TAG_SEARCH)){
	        OscMessage result = new OscMessage(TAG_RESULT);
	        for(String s: localParams.keySet()) result.add(prefix +"/"+s);
	        osc.send(result,netAddress);
//	        PApplet.println(netAddress + " is searching");
//	        PApplet.println(result);
	      }
	      if(m.checkAddrPattern(TAG_SUBSCRIBE)){
	        String remoteParamName = m.get(0).stringValue();
//	        PApplet.println("request to subscribe to " + remoteParamName);
	        
	        //TODO check if exists first -> handle duplicates
	        //FIXME is the reverse double linked list necessary ?
	        ArrayList<String> fields = null;
	        if(subscribers.get(netAddress) == null){
	        	fields = new ArrayList<String>();
//	        	System.out.println("no previous subscribers for field" + remoteParamName+" -> making new list");
	        }else{
//	        	System.out.println("existing subscribers for field" + remoteParamName+" -> retrieving list");
	        	fields = subscribers.get(netAddress);
	        }
	        if(fields.contains(remoteParamName)){
//	        	PApplet.println(netAddress + " already subscribed to" + remoteParamName);
	        }else{
	        	fields.add(remoteParamName);
//	        	PApplet.println(netAddress + " subscribed to" + remoteParamName);
	        	subscribers.put(netAddress, fields);//TODO test if this is needed
	        }
//	        PApplet.print("subscribers: ");
//	        PApplet.println(subscribers);
	        //reverse lookup table
	        ArrayList<NetAddress> subscribersPerField;
	        if(subscribedFields.get(remoteParamName) != null){
	        	subscribersPerField = subscribedFields.get(remoteParamName);
	        	if(subscribersPerField.contains(netAddress)){
//	        		PApplet.println(netAddress + " already LUT subscribed to" + remoteParamName);
	        	}else{
//	        		PApplet.println(remoteParamName + " associated with address " + netAddress);
	        		subscribersPerField.add(netAddress);
	        	}
	        }else{
	        	subscribersPerField = new ArrayList<NetAddress>();
	        	subscribersPerField.add(netAddress);
	        }
	        subscribedFields.put(remoteParamName, subscribersPerField);//TODO check if needed, or most efficient method of updating this
//	        PApplet.print("subscribedFields: ");
//	        printHashMap(subscribedFields);
	      }
	     
	      
	      if(m.checkAddrPattern(TAG_UNSUBSCRIBE)){
	    	  //TODO implement
	      }
	  }
	
	public void send(boolean on){
		for (String paramName : localParams.keySet()){
			String remoteParamName = prefix+(prefix.endsWith("/") ? "" : "/")+paramName;
			Parameter localParam = localParams.get(paramName);
			//TODO check for null ?
			try{
				float value = localParam.field.getFloat(parent);
				PApplet.println(paramName+":"+value);
				PApplet.println(subscribedFields.keySet().size()+" subscribers found");
				PApplet.println("fields: " + subscribedFields.keySet() + " -> " + remoteParamName);
				//*
				if(subscribedFields.get(remoteParamName) != null){//look into storing the result instead of getting twice
					ArrayList<NetAddress> subscribers = subscribedFields.get(remoteParamName);
					PApplet.println("subscribers " + subscribers );
					for(NetAddress subscriber : subscribers){
						PApplet.println("sending " + value + " to " + subscriber);
						osc.send(new OscMessage(remoteParamName,new Object[]{0,value,on ? 1 : 0}),subscriber);
					}
				}
				//*/
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public void dispose(){
		osc.dispose();
	}
	
}

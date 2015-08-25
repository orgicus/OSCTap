package tap.osc;

import netP5.NetAddress;
import oscP5.OscMessage;

public class OSCUtils {
		
	public static NetAddress getAddressFromBroadcast(OscMessage m,int port){
		String ip = m.address().substring(1);//address starts with a / so we discard it
		return new NetAddress(ip,port);
	}
}

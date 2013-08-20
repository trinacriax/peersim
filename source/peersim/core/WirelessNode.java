package peersim.core;

import peersim.config.*;
import java.awt.Point;

/**
 * This is the wireless {@link Node} class that is used to compose the
 * Wireless {@link Network}.
 * @author Alessandro Russo
 */

public class WirelessNode implements Node {


// ================= fields ========================================
// =================================================================

/** used to generate unique IDs */
private static long counterID = -1;

/**
* The protocols on this node.
*/
protected Protocol[] protocol = null;

/**
* The current index of this node in the node
* list of the {@link Network}. It can change any time.
* This is necessary to allow
* the implementation of efficient graph algorithms.
*/
private int index;

/**
* The fail state of the node.
*/
protected int failstate = Fallible.OK;

/**
* The ID of the node. It should be final, however it can't be final because
* clone must be able to set it.
*/
private long ID;
/*
 * Peer location in 2D in centimeters
 */
private Point position;
/*
 * Node range
 */
private int range;

// ================ constructor and initialization =================
// =================================================================

/** Used to construct the prototype node. This class currently does not
* have specific configuration parameters and so the parameter
* <code>prefix</code> is not used. It reads the protocol components
* (components that have type {@value peersim.core.Node#PAR_PROT}) from
* the configuration.
*/
public WirelessNode(String prefix) {
	
	String[] names = Configuration.getNames(PAR_PROT);
	CommonState.setNode(this);
	ID=nextID();
	protocol = new Protocol[names.length];
        position = new Point();
        range = -1;
	for (int i=0; i < names.length; i++) {
		CommonState.setPid(i);
		Protocol p = (Protocol) 
			Configuration.getInstance(names[i]);
		protocol[i] = p; 
	}
}


// -----------------------------------------------------------------

public Object clone() {
	
	WirelessNode result = null;
	try { result=(WirelessNode)super.clone(); }
	catch( CloneNotSupportedException e ) {} // never happens
	result.protocol = new Protocol[protocol.length];
        result.position = new Point();
        result.range = -1;
	CommonState.setNode(result);
	result.ID=nextID();
	for(int i=0; i<protocol.length; ++i) {
		CommonState.setPid(i);
		result.protocol[i] = (Protocol)protocol[i].clone();
	}
	return result;
}

// -----------------------------------------------------------------

/** returns the next unique ID */
private long nextID() {

	return counterID++;
}

// =============== public methods ==================================
// =================================================================


public void setFailState(int failState) {
	
	// after a node is dead, all operations on it are errors by definition
	if(failstate==DEAD && failState!=DEAD) throw new IllegalStateException(
		"Cannot change fail state: node is already DEAD");
	switch(failState)
	{
		case OK:
			failstate=OK;
			break;
		case DEAD:
			//protocol = null;
			index = -1;
			failstate = DEAD;
			for(int i=0;i<protocol.length;++i)
				if(protocol[i] instanceof Cleanable)
					((Cleanable)protocol[i]).onKill();
			break;
		case DOWN:
			failstate = DOWN;
			break;
		default:
			throw new IllegalArgumentException(
				"failState="+failState);
	}
}

// -----------------------------------------------------------------

public int getFailState() { return failstate; }

// ------------------------------------------------------------------

public boolean isUp() { return failstate==OK; }

// -----------------------------------------------------------------

public Protocol getProtocol(int i) { return protocol[i]; }

//------------------------------------------------------------------

public int protocolSize() { return protocol.length; }

//------------------------------------------------------------------

public int getIndex() { return index; }

//------------------------------------------------------------------

public void setIndex(int index) { this.index = index; }
	
//------------------------------------------------------------------

public Point getPosition() { return position; }

//------------------------------------------------------------------

public void setPosition(int x, int y ) {this.position.setLocation(x, y);
 }
	
//------------------------------------------------------------------

public int getRange() { return range; }

//------------------------------------------------------------------

public void setRange(int _range ) {this.range = _range;}

/**
* Returns the ID of this node. The IDs are generated using a counter
* (i.e. they are not random).
*/
public long getID() { return ID; }

//------------------------------------------------------------------

public String toString() 
{
	StringBuffer buffer = new StringBuffer();
	buffer.append("ID: "+ID+" index: "+index+" Position("+this.position.getX()+","+this.position.getY()+"), Range "+ this.range +"\n");
	for(int i=0; i<protocol.length; ++i)
	{
		buffer.append("protocol["+i+"]="+protocol[i]+"\n");
	}
	return buffer.toString();
}

//------------------------------------------------------------------

/** Implemented as <code>(int)getID()</code>. */
public int hashCode() { return (int)getID(); }

}




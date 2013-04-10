package com.victor.iot;

import java.util.Vector;

public class Node {
	public static final int ZC = 0;
	public static final int ZR = 1;
	public static final int ZED = 2;
	int type;
	String addr; /* as node identify */
	Vector<Endpoint> endpoints;
}

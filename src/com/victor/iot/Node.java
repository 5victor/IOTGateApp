package com.victor.iot;

import java.util.Vector;

import android.os.Parcel;
import android.os.Parcelable;

public class Node implements Parcelable {
	public static final int ZC = 0;
	public static final int ZR = 1;
	public static final int ZED = 2;
	int type;
	int nwkaddr;
	String ieeeaddr; /* as node identify */
	int epnum;
	Vector<Endpoint> endpoints;
	
	public Node()
	{
		
	}
	
	public Node(Parcel source)
	{
		this.readFromParcel(source);
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		// TODO Auto-generated method stub
		dest.writeInt(type);//int type;
		dest.writeInt(nwkaddr);//int nwkaddr;
		dest.writeString(ieeeaddr);//String ieeeaddr; /* as node identify */
		dest.writeInt(epnum);//int epnum;
	}
	
	public static final Creator<Node> CREATOR = new Creator<Node>() {
        public Node createFromParcel(Parcel source) {
            return new Node(source);
        }

        public Node[] newArray(int size) {
            return new Node[size];
        }
	};

	public void readFromParcel(Parcel src) {
		// TODO Auto-generated method stub
		type = src.readInt();//int type;
		nwkaddr = src.readInt();//int nwkaddr;
		ieeeaddr = src.readString();//String ieeeaddr; /* as node identify */
		epnum = src.readInt();//int epnum;
		
	}
	
	
}

package com.victor.iot;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Endpoint implements Parcelable {
	private static final int MAX_CLUSTER = 32;
	int index;
	int nwkaddr;
	int profileid;
	int deviceid;
	int inclusternum;
	int inclusterlist[];
	int outclusternum;
	int outclusterlist[];
	
	public Endpoint()
	{
		
	}
	
	public Endpoint(Parcel source)
	{
		this.readFromParcel(source);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(index);
		dest.writeInt(nwkaddr); //nwkaddr = source.readInt();
		dest.writeInt(profileid);//profileid = source.readInt();
		dest.writeInt(deviceid);//deviceid = source.readInt();
		dest.writeInt(inclusternum);//inclusternum = source.readInt();
		dest.writeIntArray(inclusterlist);//inclusterlist = new int[inclusternum];
		//source.readIntArray(inclusterlist);
		dest.writeInt(outclusternum);//outclusternum = source.readInt();
		dest.writeIntArray(outclusterlist);//outclusterlist = new int[outclusternum];
		//source.readIntArray(outclusterlist);
	}
	
	public static final Creator<Endpoint> CREATOR = new Creator<Endpoint>() {
        public Endpoint createFromParcel(Parcel source) {
            return new Endpoint(source);
        }

        public Endpoint[] newArray(int size) {
            return new Endpoint[size];
        }
	};

	public void readFromParcel(Parcel source) {
		// TODO Auto-generated method stub
		index = source.readInt();
		nwkaddr = source.readInt();
		profileid = source.readInt();
		deviceid = source.readInt();
		inclusternum = source.readInt();
		inclusterlist = new int[inclusternum];
		source.readIntArray(inclusterlist);
		outclusternum = source.readInt();
		outclusterlist = new int[outclusternum];
		source.readIntArray(outclusterlist);
	}
}

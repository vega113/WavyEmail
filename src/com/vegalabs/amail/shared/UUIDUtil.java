package com.vegalabs.amail.shared;

import java.util.Date;

public class UUIDUtil {
	
	 /**
     * The last time value. Used to remove duplicate UUIDs.
     */
    private static long lastTime = Long.MIN_VALUE;
    /**
     * The current clock and node value.
     */
    private static long clockSeqAndNode = 0x8000000000000000L;
    
    
	
	/**
     * Creates a new time field from the given timestamp. Note that even identical
     * values of <code>currentTimeMillis</code> will produce different time fields.
     * 
     * @param currentTimeMillis the timestamp
     * @return a new time value
     * @see UUID#getTime()
     */
    private static synchronized long createTime(long currentTimeMillis) {

        long time;

        // UTC time

        long timeMillis = (currentTimeMillis * 10000) + 0x01B21DD213814000L;

        if (timeMillis > lastTime) {
            lastTime = timeMillis;
        }
        else {
            timeMillis = ++lastTime;
        }

        // time low

        time = timeMillis << 32;

        // time mid

        time |= (timeMillis & 0xFFFF00000000L) >> 16;

        // time hi and version

        time |= 0x1000 | ((timeMillis >> 48) & 0x0FFF); // version 1

        return time;

    }
    
    public static String genUUID(){
    	clockSeqAndNode |= (long) (Math.random() * 0x3FFF) << 48;
    	String uuid = String.valueOf( createTime((new Date()).getTime())) + String.valueOf( clockSeqAndNode);
    	return uuid;
    }

}

package com.lcssit.icms.socket.pack;

import com.lcssit.icms.socket.pack.i.IPackage;

public abstract class IDataPackage extends IPackage {
    public abstract void full(String bodyStr);
}

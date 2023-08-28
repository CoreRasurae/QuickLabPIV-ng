// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.util;

public final class Entry {
    private short i;
    private short j;
    private float value;
    private Entry next;
    
    public short getI() {
        return i;
    }
    
    public short getJ() {
        return j;
    }
    
    public float getValue() {
        return value;
    }
    
    void setValue(float _value) {
       value = _value;  
    }
    
    void setI(short _i) {
        i = _i;
    }
    
    void setJ(short _j)  {
        j = _j;
    }
    
    Entry getNext() {
        return next;
    }
    
    void setNext(Entry _next) {
        next = _next;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(30);
        sb.append("I=");
        sb.append(i);
        sb.append(", J=");
        sb.append(j);
        sb.append(", Val=");
        sb.append(value);
        
        return sb.toString();
    }
}

// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.util;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import pt.quickLabPIV.util.Entry;
import pt.quickLabPIV.util.SimpleFixedLengthFloatLinkedList;

public class SimpleFixedLengthFloatLinkedListTests {


    @Test
    public void simpleInsertAndIteratorTest() {
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.add(30, (short)1, (short)5);
        
        assertEquals("Number of elements in the list doesn't match", 1, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            assertEquals("I must equal 1", 1, e.getI());
            assertEquals("J must equal 5", 5, e.getJ());
            assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            loops++;
        }
        assertEquals("Number of iterations should be just 1", 1, loops);
    }
    
    @Test
    public void simpleTwoInsertAndIteratorTest() {
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.add(30, (short)1, (short)5);
        list.add(50, (short)3, (short)1);
        
        assertEquals("Number of elements in the list doesn't match", 2, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 2", 2, loops);
    }
    
    @Test
    public void simpleThreeInsertAndIteratorTest() {
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.add(30, (short)1, (short)5);
        list.add(50, (short)3, (short)1);
        list.add(70, (short)8, (short)9);
        
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 9", 9, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 3", 3, loops);
    }

    @Test
    public void simpleThreeInsertInAscendingOrder() {
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)1, (short)5);
        list.addInAscendOrder(30, (short)3, (short)1);
        list.addInAscendOrder(50, (short)8, (short)9);
        
        
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 9", 9, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 3", 3, loops);        
    }

    @Test
    public void simpleFourInsertInAscendingOrderWithDiscard1() {
        //Replace middle element
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)1, (short)5);
        list.addInAscendOrder(30, (short)3, (short)1);
        list.addInAscendOrder(50, (short)8, (short)9);
        list.addInAscendOrder(55, (short)8, (short)8);
        
        
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 8", 8, e.getJ());
                assertEquals("Value must equal 55.0f", 55.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 3", 3, loops);
    }

    @Test
    public void simpleFourInsertInAscendingOrderWithDiscard2() {
        //Replace first element
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)1, (short)5);
        list.addInAscendOrder(30, (short)3, (short)1);
        list.addInAscendOrder(50, (short)8, (short)9);
        list.addInAscendOrder(35, (short)4, (short)1);
        
        
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 4", 4, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 35.0f", 35.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 9", 9, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 3", 3, loops);
    }

    @Test
    public void simpleFourInsertInAscendingOrderWithDiscard3() {
        //Replace last element
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)1, (short)5);
        list.addInAscendOrder(30, (short)3, (short)1);
        list.addInAscendOrder(50, (short)8, (short)9);
        list.addInAscendOrder(75, (short)1, (short)6);
        
        
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 9", 9, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 6", 6, e.getJ());
                assertEquals("Value must equal 75.0f", 75.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
    }
        
    @Test
    public void simpleFourInsertInAscendingOrderWith2Discard1() {
        //Delete first two and add before 3 (70.0)
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)7, (short)1);
        list.addInAscendOrder(30, (short)3, (short)3);
        list.addInAscendOrder(50, (short)8, (short)9);
        list.addInAscendOrder(55, (short)5, (short)6);
        
        
        assertEquals("Number of elements in the list doesn't match", 2, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 5", 5, e.getI());
                assertEquals("J must equal 6", 6, e.getJ());
                assertEquals("Value must equal 55.0f", 55.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 7", 7, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 2", 2, loops);
    }

    @Test
    public void simpleFourInsertInAscendingOrderSkip1() {
        //Skip Middle insertion, because new local max value is adjacent and lower 
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)1, (short)5);
        list.addInAscendOrder(30, (short)3, (short)1);
        list.addInAscendOrder(50, (short)8, (short)9);
        list.addInAscendOrder(49, (short)7, (short)9);
        
        
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 9", 9, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 3", 3, loops);        
    }

    @Test
    public void simpleFourInsertInAscendingOrderSkip2() {
        //Skip First insertion, because new local max value is adjacent and lower 
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)1, (short)5);
        list.addInAscendOrder(30, (short)3, (short)1);
        list.addInAscendOrder(50, (short)8, (short)9);
        list.addInAscendOrder(29, (short)3, (short)2);
        
        
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 9", 9, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 3", 3, loops);        
    }

    @Test
    public void simpleFourInsertInAscendingOrderSkip3() {
        //Skip Last insertion, because new local max value is adjacent and lower 
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)1, (short)5);
        list.addInAscendOrder(30, (short)3, (short)1);
        list.addInAscendOrder(50, (short)8, (short)9);
        list.addInAscendOrder(69, (short)2, (short)4);
            
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 9", 9, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 3", 3, loops);        
    }
    
    @Test
    public void simpleFourInsertInAscendingOrderSkip4() {
        //Skip Last insertion, because new local max value is adjacent and equal 
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        list.addInAscendOrder(70, (short)1, (short)5);
        list.addInAscendOrder(30, (short)3, (short)1);
        list.addInAscendOrder(50, (short)8, (short)9);
        list.addInAscendOrder(70, (short)2, (short)4);
            
        assertEquals("Number of elements in the list doesn't match", 3, list.size());
        
        Iterator<Entry> iter = list.iterator();
        int loops = 0;
        while (iter.hasNext()) {
            Entry e = iter.next();
            if (loops == 0) {
                assertEquals("I must equal 3", 3, e.getI());
                assertEquals("J must equal 1", 1, e.getJ());
                assertEquals("Value must equal 30.0f", 30.0f, e.getValue(), 1.0-9f);
            } else if (loops == 1) {
                assertEquals("I must equal 8", 8, e.getI());
                assertEquals("J must equal 9", 9, e.getJ());
                assertEquals("Value must equal 50.0f", 50.0f, e.getValue(), 1.0-9f);                
            } else if (loops == 2) {
                assertEquals("I must equal 1", 1, e.getI());
                assertEquals("J must equal 5", 5, e.getJ());
                assertEquals("Value must equal 70.0f", 70.0f, e.getValue(), 1.0-9f);                
            }
            loops++;
        }
        
        assertEquals("Number of iterations should be just 3", 3, loops);        
    }

    @Test
    public void simpleItemInsertionAndRemovalTest() {
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        assertEquals("List should be empty", 0,  list.size());
        
        list.addInAscendOrder(70, (short)2, (short)3);
        assertEquals("List should have one element", 1,  list.size());
        
        list.remove();
        assertEquals("List should be empty again", 0,  list.size());
    }
    
    @Test
    public void clearAllTest() {
        SimpleFixedLengthFloatLinkedList list = new SimpleFixedLengthFloatLinkedList(3, 5);
        assertEquals("List should be empty", 0,  list.size());
        
        list.addInAscendOrder(70, (short)1, (short)5);
        assertEquals("List should be empty", 1,  list.size());
        
        list.addInAscendOrder(30, (short)3, (short)1);
        assertEquals("List should be empty", 2,  list.size());
        
        list.addInAscendOrder(50, (short)8, (short)9);
        assertEquals("List should be empty", 3,  list.size());
        
        list.clear();
        assertEquals("List should be empty again", 0,  list.size());
    }
}

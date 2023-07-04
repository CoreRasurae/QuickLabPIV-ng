package pt.quickLabPIV.util;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;

public final class SimpleFixedLengthFloatLinkedList implements Iterable<Entry>, Collection<Entry> {
    private int size = 0;
    private final int maxSize;
    private final float kernelDistance;
    private Entry first = null;
    private Entry last = null;
    private Entry removed = null;
    private final PeakDeletion[] scheduledDeletions;
    
    private final class PeakDeletion {
    	Entry toDelete;
    	Entry previous;
    }
    
    public class IteratorEntry implements Iterator<Entry> {
        private Entry iter;
        
        public IteratorEntry() {
            iter = first;
        }
        
        @Override
        public boolean hasNext() {
            return iter == null ? false : true;
        }

        @Override
        public Entry next() {
            Entry result = iter;
            iter = iter.getNext();
            return result;
        }
        
    }
    
    private static float computeDistance(final short i, final short j, final Entry e ) {
    	return (float)(FastMath.pow(i - e.getI(), 2) + FastMath.pow(j - e.getJ(), 2));
    }

    private boolean isNewLowerOrEqualAdjacent(final float value, final short i, final short j, final Entry e) {
        if (computeDistance(i, j, e) < kernelDistance && value <= e.getValue() ) {
            return true;
        }
        
        return false;
    }

    private boolean isToRemoveAdjacent(final Entry e, final float value, final short i, final short j) {
        if (computeDistance(i, j, e) < kernelDistance && value > e.getValue() ) {
            return true;
        }
        
        return false;
    }
    
    public SimpleFixedLengthFloatLinkedList(int _maxSize, int _kernelWidth) {
        maxSize = _maxSize;
        kernelDistance = (_kernelWidth - 1) * _kernelWidth;
        scheduledDeletions = new PeakDeletion[maxSize];
        for (int i = 0; i < maxSize; i++) {
        	scheduledDeletions[i] = new PeakDeletion();
        }
    }
    
    private Entry getFreeElement() {
        Entry free = removed;
        removed = null;
        if (free == null) {
            free = new Entry();
        }        
        
        return free;
    }
        
    public boolean add(float value, short i, short j) {
        Entry e = getFreeElement();
        e.setValue(value);
        e.setI(i);
        e.setJ(j);
        e.setNext(null);
        
        return add(e);
    }

    public boolean addInAscendOrder(float value, short i, short j) {
        if (first == null || last == null) {
        	if (size > 0) {
        		throw new SimpleFixedLengthFloatLinkedListException("While inserting first list element, list size claims to have: " + size + " elements");
        	}
            Entry e = getFreeElement();    
            e.setValue(value);
            e.setI(i);
            e.setJ(j);
            e.setNext(null);
           
            first = e;
            last = e;
            size = 1;
        } else {            
            Entry previous = null;
            Entry iter = first;
            //Find insertion position - note that at least one element exists in the list
            //It can be interesting to look for "local" peaks that are purely adjacent (i.e. for adjacent local maximum search windows that split the peak in two)
            //Adjacent peak requires deletion of existing element and insertion of new into proper position, which can only be at same position or after current element
            //(greater than current peak value)
            int deleteSize = 0;
            while (iter != null && iter.getValue() < value) {
                if (isToRemoveAdjacent(iter, value, i, j)) {
                	//Mark candidate peaks to delete
                	PeakDeletion deletion = scheduledDeletions[deleteSize++];
                	deletion.previous = previous;
                	deletion.toDelete = iter;
                } 
                
                previous = iter;                
                iter = iter.getNext();
            }         
            
            if (size == maxSize && iter == first) {
            	return false;
            }
            
            //Before inserting, check the rest of the list for adjacent
            //(The current peak can also be adjacent to other already inserted peaks, 
            //even with greater value than the current under test)
            while (iter != null) {
                if (isNewLowerOrEqualAdjacent(value, i, j, iter)) {
                    return false;
                }
                iter = iter.getNext();
            }

            //Only perform deletion if insertion of new peak is to be made, in order to keep
            //peak consistency.
            //At this point the insertion is known to be needed for sure, so deletions can be done.
            for (int deleteIndex = 0; deleteIndex < deleteSize; deleteIndex++) {            	
                //Delete adjacent entry while iterating
                Entry deleted = scheduledDeletions[deleteIndex].toDelete;
                Entry deletedPrevious = scheduledDeletions[deleteIndex].previous;
                if (deleted == first) {                    
                    if (first == last) {
                        first = null;
                        last = null;
                        //Important... also ensure previous is updated
                        previous = null; //ADD UNIT test for this
                    } else {
                        first = first.getNext();
                        if (previous == deleted) {
                        	previous = deletedPrevious;
                        }                        
                    }
                    
                    removed = deleted;
                } else {                   
                    deletedPrevious.setNext(deleted.getNext());
                    if (last == deleted) {
                        last = deletedPrevious;
                    }
                    if (previous == deleted) {
                    	previous = deletedPrevious;
                    }
                }
                
                //In case of consecutive deletes, make sure next delete correctly refers to previous entry
                if (deleteIndex+1 < deleteSize) {
                	if (deleted == scheduledDeletions[deleteIndex+1].previous) {
                		scheduledDeletions[deleteIndex+1].previous = deletedPrevious;
                	}
                }
                //Place deleted element in deleted entries
                deleted.setI((short)-1);
                deleted.setJ((short)-1);
                deleted.setValue(0.0f);
                deleted.setNext(null);
                size--;
            }
            
            //Insert new element
            if (previous != null) {
                Entry newEntry = getFreeElement();
                newEntry.setValue(value);
                newEntry.setI(i);
                newEntry.setJ(j);
                newEntry.setNext(previous.getNext());
                previous.setNext(newEntry);
                if (previous == last) {
                    last = newEntry;
                }
                if (first == null || last == null) {
                	throw new SimpleFixedLengthFloatLinkedListException("While inserting element in the list, already containing:"  + size + " elements, first was null or last was null");
                }
                
                size++;
                if (size > maxSize) {
                    //Remove smaller size, which is now out of the top 3
                    remove();
                }                
            } else {
            	//This is not an issue, because this is only possible if no removal was performed,
            	//so insertion can be safely rejected without causing inconsistent state. 
                if (size == maxSize) {
                    return false;
                }

                Entry e = getFreeElement();    
                e.setValue(value);
                e.setI(i);
                e.setJ(j);
                e.setNext(null);

                if (first == null || last == null) {
                    first = e;
                    last = e;
                } else {
                    e.setNext(first);
                    first = e;
                }
                
                size++;
            }          
        }
        
        return true;
    }

   public float remove() {
       float value; 
       if (first == null) {
           throw new RuntimeException("Shouldn't try to remove on empty list");
       }
       
       value = first.getValue();
       removed = first;
       if (first == last) {
           first = null;
           last = null;
       } else {           
           first = first.getNext();
       }
       removed.setNext(null);
       
       size--;
       
       return value;
   }

    @Override
    public Iterator<pt.quickLabPIV.util.Entry> iterator() {
        return new IteratorEntry();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0 ? true : false;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        
        if (o.getClass() != Entry.class) {
            return false;
        }
        
        IteratorEntry iter = new IteratorEntry();
        while (iter.hasNext()) {
            if (o == iter || iter.next().equals(o)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Object[] toArray() {
        Entry[] arr = new Entry[size];

        int index = 0;
        IteratorEntry iter = new IteratorEntry();
        while (iter.hasNext()) {
            arr[index++] = iter.next();
        }
        
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        /*if (a.length < size) {
            a = new T[size];
        }*/
        
        
        return null;
    }

    @Override
    public boolean add(Entry e) {
        if (size == maxSize) {
            return false;
        }
        
        if (first == null || last == null) {
            first = e;
            last = e;
        } else if (last != null) {
            last.setNext(e);
            last = e;
        }
        
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Entry> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        while (!isEmpty()) {
            remove();
        }   
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        IteratorEntry iter = new IteratorEntry();
        sb.append("[");
        while (iter.hasNext()) {
            sb.append("<");
            sb.append(iter.next().toString());
            sb.append(">");
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        
        return sb.toString();
    }
}

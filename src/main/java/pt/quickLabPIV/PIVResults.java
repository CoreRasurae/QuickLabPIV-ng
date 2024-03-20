// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PIVResults {
    private List<PIVMap> maps = new ArrayList<PIVMap>(5);
    private boolean denseMap = false;
    
    public PIVMap getOrCreateMap(int index) {
        if (maps.size() <= index) {
            maps.add(index, new PIVMap());
        }
        PIVMap map = maps.get(index);
        
        denseMap = map.isDenseExport();
        
        return map;
    }

    public List<PIVMap> getAllMaps() {
        return Collections.unmodifiableList(maps);
        
    }

    public void concatenate(PIVResults otherPartialResults) {
        int numberOfAdaptiveMaps = maps.size();
        int numberOfOtherAdaptiveMaps = otherPartialResults.maps.size();
        
        if (numberOfAdaptiveMaps != numberOfOtherAdaptiveMaps) {
            throw new PIVConcatException("Cannot concatenate PIVResults with different structures");
        }
        
        Iterator<PIVMap> localMapsIterator = maps.iterator();
        Iterator<PIVMap> otherMapsIterator = otherPartialResults.maps.iterator();       
        while (localMapsIterator.hasNext() && otherMapsIterator.hasNext()) {
            PIVMap localMap = localMapsIterator.next();
            PIVMap otherMap = otherMapsIterator.next();
            
            if (!localMap.isConcatCompatible(otherMap)) {
                throw new PIVConcatException("Maps to concatenate are not compatible");
            }
        }

        localMapsIterator = maps.iterator();
        otherMapsIterator = otherPartialResults.maps.iterator();        
        while (localMapsIterator.hasNext() && otherMapsIterator.hasNext()) {
            PIVMap localMap = localMapsIterator.next();
            PIVMap otherMap = otherMapsIterator.next();
            
            localMap.concatenate(otherMap);
        }

    }

    public boolean isDenseMap() {
        return denseMap || maps.get(0).isDenseExport();
    }

    public void clear() {
        for (PIVMap map : maps) {
            map.clear();
        }
        maps = null;        
    }
}

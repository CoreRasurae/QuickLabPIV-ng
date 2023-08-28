// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

import java.util.Iterator;

import pt.quickLabPIV.iareas.IterationStepTiles;
import pt.quickLabPIV.iareas.Tile;

public class PIVMap implements Iterable<Velocities> {
    private int iaWidth;
    private int iaHeight;
    
    private int imageWidth;
    private int imageHeight;
    
    private int marginTop;
    private int marginLeft;
    private int marginRight;
    private int marginBottom;
    
    /** Batch absolute frame offset - when splitting across multiple files */
    private int absoluteFrameOffset;
    /** Batch relative frame offset - when splitting across multiple files */
    private int relativeFrameOffset;
    private int numberOfMaps;
    private int mapHeight;
    private int mapWidth;
    private Velocities[] velocityMaps;
    private PIVMap  nextMap;
    
    private boolean denseExport = false;
    private boolean prepared = false;
    
    private boolean swapUVOrder = false;
    private boolean markInvalidAsNaN = false;
    
    public PIVMap() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        Object configurationObject = singleton.getPIVParameters().getSpecificConfiguration(PIVMapOptionalConfiguration.IDENTIFIER);
        if (configurationObject != null) {
            PIVMapOptionalConfiguration optional = (PIVMapOptionalConfiguration)configurationObject;
            markInvalidAsNaN = optional.isMarkInvalidAsNaN();
            swapUVOrder = optional.isSwapUVOrder();
        }
    }
    
    public void prepare(int _absoluteFrameOffset, int _relativeFrameOffset, int _numberOfMaps, int _imageHeight, int _imageWidth, IterationStepTiles iterStepTiles) {
        absoluteFrameOffset = _absoluteFrameOffset;
        relativeFrameOffset = _relativeFrameOffset;
        
        numberOfMaps = _numberOfMaps;
        mapHeight = iterStepTiles.getNumberOfTilesInI();
        mapWidth = iterStepTiles.getNumberOfTilesInJ();
        velocityMaps = new Velocities[numberOfMaps];
        iaHeight = iterStepTiles.getTileHeight();
        iaWidth = iterStepTiles.getTileWidth();
        marginTop = iterStepTiles.getMarginTop();
        marginLeft = iterStepTiles.getMarginLeft();
        marginRight = iterStepTiles.getMarginRight();
        marginBottom = iterStepTiles.getMarginBottom();
        
        imageWidth = _imageWidth;
        imageHeight = _imageHeight;        
        
        prepared = true;
    }

    
    void makeCompatWithMap(int numberOfMaps, PIVMap compatMap) {
        iaHeight = compatMap.iaHeight;
        iaWidth = compatMap.iaWidth;
        marginLeft = compatMap.marginLeft;
        marginRight = compatMap.marginRight;
        marginBottom = compatMap.marginBottom;
        marginTop = compatMap.marginTop;
        imageHeight = compatMap.imageHeight;
        imageWidth = compatMap.imageWidth;
        velocityMaps = new Velocities[0];
        
        prepared = true;
    }
    
    public void importFromIterationStepTiles(int currentFrame, final IterationStepTiles iterStepTiles) {
        if (!prepared) {
            //TODO throw Exception
        }
        
        final int relFrameNumber = currentFrame - relativeFrameOffset;
        
        if (velocityMaps[relFrameNumber] != null) {
            throw new InvalidPIVMapException("Trying to overwrite existing velocity map");
        }
    
        if (iterStepTiles.isDenseTiles()) {
            denseExport = true;
            
            Velocities velocityMap = new Velocities(currentFrame, iterStepTiles.getDenseHeight(), iterStepTiles.getDenseWidth());
            velocityMaps[relFrameNumber] = velocityMap;
            float[][] velocitiesU = velocityMap.getU();
            float[][] velocitiesV = velocityMap.getV();

            float[] uBuffer = iterStepTiles.getUBuffer();
            float[] vBuffer = iterStepTiles.getVBuffer();
            for (int i = 0; i < iterStepTiles.getDenseHeight(); i++) {
                for (int j = 0; j < iterStepTiles.getDenseWidth(); j++) {
                    final int idx = i * iterStepTiles.getDenseWidth() + j;
                    float u = uBuffer[idx];
                    float v = vBuffer[idx];
                    velocitiesU[i][j] = swapUVOrder ? u : v;
                    velocitiesV[i][j] = swapUVOrder ? v : u;
                }
            }
        } else {
            Velocities velocityMap = new Velocities(currentFrame, mapHeight, mapWidth);
            
            velocityMaps[relFrameNumber] = velocityMap;
            float[][] velocitiesU = velocityMap.getU();
            float[][] velocitiesV = velocityMap.getV();
        
            for (int indexI = 0; indexI < mapHeight; indexI++) {
                for (int indexJ = 0; indexJ < mapWidth; indexJ++) {
                    Tile tile = iterStepTiles.getTile(indexI, indexJ);
                    if (markInvalidAsNaN && (tile.isInvalidDisplacement() || tile.isMaskedDisplacement())) {
                        velocitiesU[indexI][indexJ] = Float.NaN;
                        velocitiesV[indexI][indexJ] = Float.NaN;
                    } else {
                        velocitiesU[indexI][indexJ] = swapUVOrder ? tile.getDisplacementU() : tile.getDisplacementV();
                        velocitiesV[indexI][indexJ] = swapUVOrder ? tile.getDisplacementV() : tile.getDisplacementU();                    
                    }
                }
            }
        }
    }
    
    public boolean isConcatCompatible(PIVMap otherMap) {
        //Just check for non-overlap, but allow uncontiguous...     
        if (otherMap.numberOfMaps > 0 && relativeFrameOffset + numberOfMaps > otherMap.relativeFrameOffset) {
            throw new PIVConcatException("Maps to concatenate have frames that overlap");
        }
        
        if (mapHeight != otherMap.mapHeight || mapWidth != otherMap.mapWidth) {
            throw new PIVConcatException("Maps to concatenate have different velocity maps geometries");
        }
        
        if (iaHeight != otherMap.iaHeight || iaWidth != otherMap.iaWidth) {
            throw new PIVConcatException("Maps to concatenate have different interrogation areas geometries");
        }
        
        if (marginTop != otherMap.marginTop && marginLeft != otherMap.marginLeft ||
            marginBottom != otherMap.marginBottom && marginRight != otherMap.marginRight) {
            throw new PIVConcatException("Maps have different margins");
        }
        
        return true;
    }
    
    public void concatenate(final PIVMap otherMap) {
        if (!isConcatCompatible(otherMap)) {
            throw new PIVConcatException("Maps to concatenate are not compatible");
        }
        
        nextMap = otherMap;
    }
    
    public boolean isPrepared() {
        return prepared;
    }
    
    public int getAbsoluteFrameOffset() {
        return absoluteFrameOffset;
    }
    
    public int getRelativeFrameOffset() {
        return relativeFrameOffset;
    }
    
    public int getNumberOfMaps() {
        int totalNumberOfMaps = numberOfMaps;
        PIVMap next = nextMap;
        while (next != null) {
            totalNumberOfMaps += next.numberOfMaps;
            next = next.nextMap;
        }
        return totalNumberOfMaps;
    }
    
    public int getHeight() {
        return mapHeight;
    }
    
    public int getWidth() {
        return mapWidth;
    }
    
    public int getIAHeight() {
        return iaHeight;
    }
    
    public int getIAWidth() {
        return iaWidth;
    }
    
    public int getImageHeight() {
        return imageHeight;
    }
    
    public int getImageWidth() {
        return imageWidth;
    }

    public int getMarginTop() {
        return marginTop;
    }
    
    public int getMarginLeft() {
        return marginLeft;
    }
    
    public int getMarginRight() {
        return marginRight;
    }
    
    public int getMarginBottom() {
        return marginBottom;
    }

    public boolean isDenseExport() {
        return denseExport;
    }
    
    public Iterator<Velocities> iterator() {
        Iterator<Velocities> result = new Iterator<Velocities>() {
            private PIVMap currentMap;
            private int currentFrameIndex = 0;
            
            Iterator<Velocities> setInitialPIVMap(final PIVMap initial) {
                currentMap = initial;
                return this;
            }
            
            @Override
            public boolean hasNext() {
                if (currentMap.nextMap != null || currentMap.numberOfMaps > currentFrameIndex) {
                    return true;
                }
                
                return false;
            }

            @Override
            public Velocities next() {
                Velocities result = null;
                if (currentMap != null && currentMap.numberOfMaps > currentFrameIndex) {
                    result = currentMap.velocityMaps[currentFrameIndex];
                    
                    //FIXME Iterator may point to a null entry.... due to partially processed PIV
                    if (result == null) {
                        throw new InvalidPIVMapException("Not all velocity maps have been filled in");
                    }
                }

                //Advance to next               
                if (currentMap != null && currentMap.numberOfMaps >= currentFrameIndex) {
                    currentFrameIndex++;
                    if (currentMap.numberOfMaps <= currentFrameIndex && currentMap.nextMap != null) {
                        currentMap = currentMap.nextMap;
                        currentFrameIndex = 0;
                    }                   
                }
                
                return result;
            }
            
        }.setInitialPIVMap(this);
        
        return result;
    }

    public void clear() {
        for (Velocities velocityMap : velocityMaps) {
            velocityMap.clear();
        }
        velocityMaps = null;
    }       
}

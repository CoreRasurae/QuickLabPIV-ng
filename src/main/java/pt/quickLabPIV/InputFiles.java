// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

public class InputFiles {
	private int absoluteStartFrame;
	private int relativeStartFrame;
	private List<File> inputFilesA;
	private List<File> inputFilesB;
	
	public InputFiles(int _absoluteStartFrame, int _relativeStartFrame, List<File> _inputFilesA, List<File> _inputFilesB) {
		if (_inputFilesA.size() != _inputFilesB.size()) {
			throw new InvalidPIVParametersException("Number of inputFilesA must match number of inputFilesB");
		}
		
		inputFilesA = _inputFilesA;
		inputFilesB = _inputFilesB;
		absoluteStartFrame = _absoluteStartFrame;
		relativeStartFrame = _relativeStartFrame;
	}
	
	public List<File> getFilesA() {
		return inputFilesA;
	}
	
	public List<File> getFilesB() {
		return inputFilesB;
	}
	
	public int getAbsoluteStartFrame() {
		return absoluteStartFrame;
	}
	
	public int getRelativeStartFrame() {
	    return relativeStartFrame;
	}
	
	private List<InputFiles> computeOpenCLFilesDistribution() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVRunParameters runParameters = singleton.getPIVRunParameters();
	    
        int numThreads = runParameters.getTotalNumberOfThreads();
        if (inputFilesA.size() < numThreads) {
            numThreads = inputFilesA.size();
        }

        List<InputFiles> splittedInputFiles = new ArrayList<InputFiles>(numThreads);
        Collection<DeviceRuntimeConfiguration> configs = runParameters.getDeviceConfigurations();
        
        int cpuThreadsForDevice[] = new int[configs.size()];
        
        int threadsInUse = 0;
        int i = 0;
        for (DeviceRuntimeConfiguration config : configs) {
        	int threadsInUseForDevice = 0;
            for (int threadIdx : config.getCpuThreadAssignments()) {
                if (threadIdx < numThreads) {
                    threadsInUse++;
                    threadsInUseForDevice++;
                }
                
                if (threadsInUse == numThreads) {
                    break;
                }
            }
            if (threadsInUseForDevice > 0) {
            	cpuThreadsForDevice[i] = threadsInUseForDevice;
            } else {
            	cpuThreadsForDevice[i] = 0;
            }
            i++;

            if (threadsInUse == numThreads) {
                break;
            }            
        }
        
        //Compute weights per device
        float weights[] = new float[configs.size()];
        int filesPerDevice[] = new int[configs.size()];
        float totalWeight = 0.0f;
        i = 0;
        for (DeviceRuntimeConfiguration config : configs) {
            if (cpuThreadsForDevice[i] > 0) {
                weights[i] = config.getScore() == 1.0f ? cpuThreadsForDevice[i] : ((config.getScore() - 1.0f) / 100.0f) * cpuThreadsForDevice[i];
                totalWeight += weights[i++];
        	} else {
        		weights[i++] = 0.0f;
        	}
        }
        
        for (i = 0; i < weights.length; i++) {
            weights[i] /= totalWeight;
        }
        
        //Compute files per device
        int totalFiles = 0;
        for (i = 0; i < weights.length; i++) {
            float weight = weights[i];
            filesPerDevice[i] = (int)FastMath.floor(inputFilesA.size() * weight);
            //Floor may give a value of 0, but with small amount of files we still want to distribute them in such a way that
            //they utilize all the available compute devices.
            if (weight > 0 && filesPerDevice[i] == 0) {
            	filesPerDevice[i]++;
            }
            totalFiles += filesPerDevice[i];
            if (i == filesPerDevice.length - 1) {
                if (totalFiles < inputFilesA.size()) {
                    filesPerDevice[i]++;
                    totalFiles++;
                }
            }
        }
        
        if (totalFiles != inputFilesA.size()) {
            throw new InvalidPIVParametersException("Failed to distribute files per OpenCL devices");
        }

        int filesByThreadIndex[] = new int[numThreads];
        //Split files per thread and device
        i = 0;
        for (DeviceRuntimeConfiguration config : configs) {
        	if (cpuThreadsForDevice[i] <= 0) {
        		i++;
        		continue;
        	}
        	
            int totalFilesPerDeviceThread = filesPerDevice[i];
            int filesPerDeviceThread = totalFilesPerDeviceThread / cpuThreadsForDevice[i];
            while (totalFilesPerDeviceThread > 0) {
                for (int threadId : config.getCpuThreadAssignments()) {
                    if (threadId < numThreads) {
                        filesByThreadIndex[threadId] += filesPerDeviceThread;                    
                        totalFilesPerDeviceThread -= filesPerDeviceThread;
                        if (totalFilesPerDeviceThread == 0) {
                            break;
                        }
                    }
                }
                filesPerDeviceThread = 1;
            }
            i++;
        }
        
        //At this point we have the exact number of file pairs to be assigned by thread,
        //so that each thread processes a contiguous file sequence.
        int startIndex = 0;
        int currentStartFrame = 0;
        for (i = 0; i < numThreads; i++) {
            InputFiles splitted = new InputFiles(currentStartFrame + absoluteStartFrame, currentStartFrame, 
                    inputFilesA.subList(startIndex, startIndex+filesByThreadIndex[i]),
                    inputFilesB.subList(startIndex, startIndex+filesByThreadIndex[i]));
            splittedInputFiles.add(splitted);
            startIndex+=filesByThreadIndex[i];
            currentStartFrame+=filesByThreadIndex[i];
        }
        
        return splittedInputFiles;
	}
	
	private List<InputFiles> computeJavaOnlyFilesDistribution() {
        PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
        PIVRunParameters runParameters = singleton.getPIVRunParameters();

        int numThreads = runParameters.getTotalNumberOfThreads();
        if (inputFilesA.size() < numThreads) {
            numThreads = inputFilesA.size();
        }
        List<InputFiles> splittedInputFiles = new ArrayList<InputFiles>(numThreads);
        
        int filesPerThread = inputFilesA.size() / numThreads;
        int remaining = inputFilesA.size() - filesPerThread * numThreads;
        
        int filesByThreadIndex[] = new int[numThreads];
        for (int i = 0; i < numThreads; i++) {
            filesByThreadIndex[i] = filesPerThread;
        }
        
        int threadIndex = 0;
        while (remaining > 0) {
            filesByThreadIndex[threadIndex++]++;
            remaining--;
            if (threadIndex == numThreads) {
                threadIndex = 0;
            }
        };
        
        //At this point we have the exact number of file pairs to be assigned by thread,
        //so that each thread processes a contiguous file sequence.
        int startIndex = 0;
        int currentStartFrame = 0;
        for (int i = 0; i < numThreads; i++) {
            InputFiles splitted = new InputFiles(currentStartFrame + absoluteStartFrame, currentStartFrame, 
                    inputFilesA.subList(startIndex, startIndex+filesByThreadIndex[i]),
                    inputFilesB.subList(startIndex, startIndex+filesByThreadIndex[i]));
            splittedInputFiles.add(splitted);
            startIndex+=filesByThreadIndex[i];
            currentStartFrame+=filesByThreadIndex[i];
        }
        
        return splittedInputFiles;
	}
	
	public List<InputFiles> splitIntoThreads() {
	    PIVContextSingleton singleton = PIVContextSingleton.getSingleton();
	    PIVRunParameters runParameters = singleton.getPIVRunParameters();
	    
	    if (runParameters.isUseOpenCL()) {
	        return computeOpenCLFilesDistribution();
	    } else {
	        return computeJavaOnlyFilesDistribution();
	    }
	}

    public int size() {        
        return inputFilesA.size();
    }
}

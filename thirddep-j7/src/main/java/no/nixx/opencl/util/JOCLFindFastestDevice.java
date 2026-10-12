package no.nixx.opencl.util;

/*
 * JOCL - Java bindings for OpenCL
 *
 * Copyright 2010 Marco Hutter - http://www.jocl.org/
 */


import static org.jocl.CL.CL_DEVICE_GLOBAL_MEM_SIZE;
import static org.jocl.CL.CL_DEVICE_MAX_CLOCK_FREQUENCY;
import static org.jocl.CL.CL_DEVICE_MAX_COMPUTE_UNITS;
import static org.jocl.CL.CL_DEVICE_NAME;
import static org.jocl.CL.CL_DEVICE_TYPE;
import static org.jocl.CL.CL_DEVICE_TYPE_ACCELERATOR;
import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.CL_DEVICE_TYPE_CPU;
import static org.jocl.CL.CL_DEVICE_TYPE_DEFAULT;
import static org.jocl.CL.CL_DEVICE_TYPE_GPU;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetDeviceInfo;
import static org.jocl.CL.clGetPlatformIDs;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

/**
 * A JOCL program that queries and prints information about all
 * available devices.
 */
public class JOCLFindFastestDevice
{

	public enum DeviceType {CPU(1), GPU(2), Accelerator(2), Default(1);
		DeviceType(int arg) {prio = arg;}; int prio;
	};
    /**
     * The entry point of this program
     *
     * @param args Not used
     */
    public static Device findBest()     {
        // Obtain the number of platforms
        int numPlatforms[] = new int[1];
        clGetPlatformIDs(0, null, numPlatforms);

        // Obtain the platform IDs
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms[0]];
        clGetPlatformIDs(platforms.length, platforms, null);

        // Collect all devices of all platforms
        List<Device> devices = new ArrayList<Device>();
        System.out.println("Devices:"+platforms.length);
        for (cl_platform_id platform : platforms) {
            // Obtain the number of devices for the current platform
            int numDevices[] = new int[1];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, numDevices);

            cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, numDevices[0], devicesArray, null);

            for (cl_device_id next : devicesArray) {
				devices.add(Device.as(platform, next));
			}
        }

        DeviceInfo bestDevice = null;

        // Print the infos about all devices
        for (Device deviceNext : devices) {

        	cl_device_id device = deviceNext.device;

        	DeviceInfo di = new DeviceInfo();
            // CL_DEVICE_NAME
        	di.device = deviceNext;
            di.name = getString(device, CL_DEVICE_NAME);

            // CL_DEVICE_TYPE
            long deviceType = getLong(device, CL_DEVICE_TYPE);
            if( (deviceType & CL_DEVICE_TYPE_CPU) != 0)
                di.type =  DeviceType.CPU;
            if( (deviceType & CL_DEVICE_TYPE_GPU) != 0)
            	di.type =  DeviceType.GPU;
            if( (deviceType & CL_DEVICE_TYPE_ACCELERATOR) != 0)
            	di.type =  DeviceType.Accelerator;
            if( (deviceType & CL_DEVICE_TYPE_DEFAULT) != 0)
            	di.type =  DeviceType.Default;

            // CL_DEVICE_MAX_COMPUTE_UNITS
            di.computeUnits = getInt(device, CL_DEVICE_MAX_COMPUTE_UNITS);


            // CL_DEVICE_MAX_CLOCK_FREQUENCY
            di.clockFreq = getLong(device, CL_DEVICE_MAX_CLOCK_FREQUENCY);

            // CL_DEVICE_GLOBAL_MEM_SIZE
            long globalMemSize = getLong(device, CL_DEVICE_GLOBAL_MEM_SIZE);
            di.memory = (int)(globalMemSize / (1024 * 1024));

            if (null == bestDevice || !bestDevice.isBetterThan(di)) bestDevice = di;

        }

        System.out.println("Used device: "+bestDevice);
        return bestDevice.device;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static int getInt(cl_device_id device, int paramName) { return getInts(device, paramName, 1)[0]; }

    /**
     * Returns the values of the device info parameter with the given name
     */
    private static int[] getInts(cl_device_id device, int paramName, int numValues) {
        int values[] = new int[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_int * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     */
    private static long getLong(cl_device_id device, int paramName) { return getLongs(device, paramName, 1)[0]; }

    /** Returns the values of the device info parameter with the given name   */
    private static long[] getLongs(cl_device_id device, int paramName, int numValues) {
        long values[] = new long[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_long * numValues, Pointer.to(values), null);
        return values;
    }

    /**  Returns the value of the device info parameter with the given name */
    private static String getString(cl_device_id device, int paramName) {
        long size[] = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length-1);
    }




    /** Returns the values of the device info parameter with the given name */
    static long[] getSizes(cl_device_id device, int paramName, int numValues)
    {
        // The size of the returned data has to depend on the size of a size_t, which is handled here
        ByteBuffer buffer = ByteBuffer.allocate( numValues * Sizeof.size_t).order(ByteOrder.nativeOrder());
        clGetDeviceInfo(device, paramName, Sizeof.size_t * numValues, Pointer.to(buffer), null);
        long values[] = new long[numValues];

        if (Sizeof.size_t == 4)
			for (int i=0; i<numValues; i++) values[i] = buffer.getInt(i * Sizeof.size_t);
		else
			for (int i=0; i<numValues; i++) values[i] = buffer.getLong(i * Sizeof.size_t);

        return values;
    }

    public static class Device {
    	public cl_device_id device;
    	public cl_platform_id platform;

    	final static Device as(cl_platform_id platform, cl_device_id device) {
    		Device dev = new Device();
    		dev.device = device;
    		dev.platform = platform;
    		return dev;
    	}
    }

    static class DeviceInfo {
    	String name;
    	int computeUnits;
    	long clockFreq;
    	DeviceType type;
    	int memory;
    	Device device;

    	public boolean isBetterThan(DeviceInfo di) {
    		if (this.computeUnits > di.computeUnits) return true;
    		if (this.computeUnits < di.computeUnits) return false;

    		if (this.type.prio > di.type.prio) return true;

    		if (this.clockFreq - di.clockFreq > 70) return true;
    		if (di.clockFreq - this.clockFreq > 70) return false;

    		if (this.memory > di.memory) return true;
    		return false;
    	}


    	public String toString() {
			return "DeviceInfo [name=" + name + ", computeUnits=" + computeUnits + ", clockFreq=" + clockFreq
					+ ", type=" + type + ", memory=" + memory + "]";
		}


    }

}
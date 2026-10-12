package org.delafer.xanderView.scale.deprecated;

import static no.nixx.opencl.util.BufferedImageUtils.getDataBufferInt;
import static no.nixx.opencl.util.OCLUtils.createProgramFromSource;
import static no.nixx.opencl.util.OCLUtils.createReadOnlyImage;
import static no.nixx.opencl.util.OCLUtils.createWritableImage;
import static no.nixx.opencl.util.OCLUtils.getCommandQueueForContextAndPlatformIdAndDeviceId;
import static no.nixx.opencl.util.OCLUtils.getContextForPlatformIdAndDeviceId;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadImage;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clReleaseProgram;
import static org.jocl.CL.clSetKernelArg;

import java.awt.image.BufferedImage;

import no.nixx.opencl.util.ClasspathUtils;
import no.nixx.opencl.util.JOCLFindFastestDevice;

import org.delafer.xanderView.scale.IResizer;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

/**
 * Oddbjørn Kvalsund
 */
public class ResizeOpenCL implements IResizer{

    private final cl_platform_id platformId;
    private final cl_device_id deviceId;
    private final cl_context context;
    private final cl_command_queue commandQueue;
    private final cl_program program;

	private static final class Holder {
		/** The Constant INSTANCE. */
		private final static transient ResizeOpenCL INSTANCE = new ResizeOpenCL();
	}

	/**
	 * Gets the single instance of ResourcesDR.
	 *
	 * @return single instance of ResourcesDR
	 */
	public static final ResizeOpenCL instance() {
		return Holder.INSTANCE;
	}

    private ResizeOpenCL() {
//        platformId = getFirstPlatformId();
//        deviceId = getFirstDeviceIdForPlatformId(platformId);

    	no.nixx.opencl.util.JOCLFindFastestDevice.Device device = JOCLFindFastestDevice.findBest();
    	platformId = device.platform;
    	deviceId = device.device;
        context = getContextForPlatformIdAndDeviceId(platformId, deviceId);
        commandQueue = getCommandQueueForContextAndPlatformIdAndDeviceId(context, deviceId);
        program = createProgramFromSource(context, ClasspathUtils.getClasspathResourceAsString("resize.cl"));
    }

    @SuppressWarnings("unused")
    public ResizeOpenCL(cl_platform_id platformId, cl_device_id deviceId, cl_context context, cl_command_queue commandQueue, cl_program program) {
        this.platformId = platformId;
        this.deviceId = deviceId;
        this.context = context;
        this.commandQueue = commandQueue;
        this.program = program;
    }

    public BufferedImage resize(BufferedImage inputImage, int newLongEdgeLength) {
        final int inputImageWidth = inputImage.getWidth();
        final int inputImageHeight = inputImage.getHeight();

        final int outputImageWidth;
        final int outputImageHeight;
        if(inputImageWidth > inputImageHeight) {
            outputImageWidth = newLongEdgeLength;
            outputImageHeight = inputImageHeight * (newLongEdgeLength / inputImageWidth);
        } else {
            outputImageWidth = inputImageWidth * (newLongEdgeLength / inputImageHeight);
            outputImageHeight = newLongEdgeLength;
        }

        return resize(inputImage, outputImageWidth, outputImageHeight);
    }

    public BufferedImage resize(BufferedImage inputImage, int outputImageWidth, int outputImageHeight) {
        final BufferedImage outputImage = new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_INT_RGB);

        final cl_mem inputRaster = createReadOnlyImage(context, inputImage);
        final cl_mem outputRaster = createWritableImage(context, outputImageWidth, outputImageHeight);

        // Ref. http://www.khronos.org/registry/cl/sdk/1.1/docs/man/xhtml/clSetKernelArg.html:
        // Rather than attempt to share cl_kernel objects among multiple host threads, applications are strongly
        // encouraged to make additional cl_kernel objects for kernel functions for each host thread.
        final cl_kernel kernel = clCreateKernel(program, "resizeImage", null);
        final long globalWorkSize[] = new long[2];
        globalWorkSize[0] = outputImageWidth;
        globalWorkSize[1] = outputImageHeight;
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(inputRaster));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(outputRaster));

        clEnqueueNDRangeKernel(commandQueue, kernel, 2, null, globalWorkSize, null, 0, null, null);

        final int outputData[] = getDataBufferInt(outputImage);
        clEnqueueReadImage(
                commandQueue, outputRaster, true, new long[3],
                new long[]{outputImageWidth, outputImageHeight, 1},
                outputImageWidth * Sizeof.cl_uint, 0,
                Pointer.to(outputData), 0, null, null);

        clReleaseMemObject(outputRaster);
        clReleaseMemObject(inputRaster);
        clReleaseKernel(kernel);

        return outputImage;
    }

    public void dispose() {
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }

    public String name() {
		return this.getClass().getSimpleName();
	}
}
package org.delafer.xanderView.scale;


public abstract class ResizerBase implements IResizer {


	public int getMaxFilters() {
		return 1;
	}

	public abstract ResizerBase as(int filterI);

	public abstract int current();


}

package org.delafer.xanderView.filter;

public abstract class IFilter {
	private String[] args;
	void setArgs(String[] args) {
		this.args = args;
	};
	protected abstract void initialize();
}

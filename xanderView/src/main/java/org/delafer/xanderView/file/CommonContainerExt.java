package org.delafer.xanderView.file;

import org.delafer.xanderView.comparator.RandomComparator;
import org.delafer.xanderView.file.entry.ImageAbstract;
import org.delafer.xanderView.general.State;
import org.delafer.xanderView.gui.splash.SplashWindow;
import org.delafer.xanderView.gui.helpers.MultiShell;

public class CommonContainerExt extends CommonContainer{

	private boolean randomMode;
	private boolean onlyExisting;
	int startAt;

	public CommonContainerExt(String locationArg) {
		super(locationArg);
		randomMode = false;
		startAt = this.currentIndex();
	}

	@Override
	public ImageAbstract<?> getPrevious10() {
		if (onlyExisting)
			return this.getPrevious();
		else
			return super.getPrevious10();
	}

	@Override
	public ImageAbstract<?> getNext10() {
		if (onlyExisting)
			return this.getNext();
		else
			return super.getNext10();
	}

	@Override
	public ImageAbstract<?> getNext() {
		if (!onlyExisting) {
			ImageAbstract<?> next =  super.getNext();
			checkStartReached();
			return next;
		}
		int s = this.size();
		ImageAbstract<?> ret = null;
		do {
			ret = super.getNext();
		} while (((s--)>0) && CopyService.exists(ret));
		return ret;
	}


	@Override
	public ImageAbstract<?> getPrevious() {
		if (!onlyExisting) {
			ImageAbstract<?> prev =  super.getPrevious();
			checkStartReached();
			return prev;
		}
		int s = this.size();
		ImageAbstract<?> ret = null;
		do {
			ret = super.getPrevious();
		} while (((s--)>0) && CopyService.exists(ret));

		return ret;
	}

	private void checkStartReached() {
		if (startAt == currentIndex()) {
//			SoundBeep.beep();
			new SplashWindow(MultiShell.shell.active(), State.Special1);
		}
	}

	public void switchRandomAccess() {
		this.randomMode = !randomMode;
		if (!randomMode) {
			initImagesForComparator(null);
		} else {
			initImagesForComparator(new RandomComparator());
		}
	}

	public void switchOnlyExisting() {
		this.onlyExisting = !onlyExisting;
	}

	public boolean isOnlyExisting() {
		return onlyExisting;
	}

	public boolean isRandomMode() {
		return randomMode;
	}


}

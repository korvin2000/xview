package org.delafer.xanderView.orientation;

import org.delafer.xanderView.common.ImageSize;

public class OrientationCommons {

	//public enum Action {Left, Right, TurnAbout, VerticalFlip, HorizontalFlip };

	public enum Action {RotateLeft(0), RotateRight(1), FlipVertical(2), FlipHorizontal(3);
	int code;
	Action(int n) {code = n;}
	int code() {return code;}
	};

	final static int stOriginal 		= 0;
	final static int stRotatedLeft 		= 1;
	final static int stRotatedRight 	= 2;
	final static int stTurnedAbout 		= 3;
	final static int stFlipVertical 	= 4;
	final static int stFlipHorizonal 	= 5;
	final static int stHFlipAndRight 	= 6;
	final static int stHFlipAndLeft		= 7;


	public enum Orientation {

		Original(stOriginal, stRotatedLeft, stRotatedRight, stFlipVertical, stFlipHorizonal, false),
		RotatedLeft(stRotatedLeft, stTurnedAbout, stOriginal, stHFlipAndLeft, stHFlipAndRight, true),
		RotatedRight(stRotatedRight, stOriginal, stTurnedAbout, stHFlipAndRight, stHFlipAndLeft, true),
		TurnedAbout(stTurnedAbout, stRotatedRight, stRotatedLeft, stFlipHorizonal, stFlipVertical, false),
		FlipVertical(stFlipVertical, stHFlipAndRight, stHFlipAndLeft, stOriginal, stTurnedAbout, false),
		FlipHorizonal(stFlipHorizonal, stHFlipAndLeft, stHFlipAndRight, stTurnedAbout, stOriginal, false),

		HFlipAndRight(stHFlipAndRight, stFlipHorizonal, stFlipVertical, stRotatedRight, stRotatedLeft, true),
		HFlipAndLeft(stHFlipAndLeft, stFlipVertical, stFlipHorizonal, stRotatedLeft, stRotatedRight, true);

	int[] gotos;
	boolean swapXY;
	Orientation(int ownCode, int onLeft, int onRight, int onVertical, int onHorizontal, boolean swapXY) {
		gotos = new int[] {onLeft, onRight, onVertical, onHorizontal};
		this.swapXY = swapXY;


	}

	public boolean swapXY() {return swapXY;}

	public Orientation newState(Action action) {
		int newCode = gotos[action.code];
		switch (newCode) {
		case stOriginal:
			return Orientation.Original;
		case stRotatedLeft:
			return Orientation.RotatedLeft;
		case stRotatedRight:
			return Orientation.RotatedRight;
		case stTurnedAbout:
			return Orientation.TurnedAbout;
		case stFlipVertical:
			return Orientation.FlipVertical;
		case stFlipHorizonal:
			return Orientation.FlipHorizonal;
		case stHFlipAndRight:
			return Orientation.HFlipAndRight;
		case stHFlipAndLeft:
			return Orientation.HFlipAndLeft;
		default:
			return this;
		}
	}

	};

	public static ImageSize getNewSize(int currentWidth, int currentHeight, int targetWidth, int targetHeight) {
		float requestedHeightScaling = ((float) targetHeight / (float) currentHeight);
		float requestedWidthScaling = ((float) targetWidth / (float) currentWidth);
		float actualScaling = Math.min(requestedHeightScaling, requestedWidthScaling);

		targetHeight = Math.round((float) currentHeight * actualScaling);
		targetWidth = Math.round((float) currentWidth * actualScaling);
		return new ImageSize(targetWidth, targetHeight);
	}

//	public static void main(String[] args) {
//		System.out.println(Orientation.TurnedAbout.newState(Action.FlipHorizontal));
//	}

	//left vert = right horiz = vert right = horiz left
	//vert left = horiz right = left hori = right vert


}

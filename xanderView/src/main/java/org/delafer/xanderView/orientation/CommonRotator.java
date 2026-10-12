package org.delafer.xanderView.orientation;

import java.awt.image.BufferedImage;

import org.delafer.xanderView.orientation.OrientationCommons.Orientation;


public abstract class CommonRotator {

	/*
	 * 		Original(stOriginal, stRotatedLeft, stRotatedRight, stFlipVertical, stFlipHorizonal),
		RotatedLeft(stRotatedLeft, stTurnedAbout, stOriginal, stHFlipAndLeft, stHFlipAndRight),
		RotatedRight(stRotatedRight, stOriginal, stTurnedAbout, stHFlipAndRight, stHFlipAndLeft),
		TurnedAbout(stTurnedAbout, stRotatedRight, stRotatedLeft, stFlipHorizonal, stFlipVertical),
		FlipVertical(stFlipVertical, stHFlipAndRight, stHFlipAndLeft, stOriginal, stTurnedAbout),
		FlipHorizonal(stFlipHorizonal, stHFlipAndLeft, stHFlipAndRight, stTurnedAbout, stOriginal),

		HFlipAndRight(stHFlipAndRight, stFlipHorizonal, stFlipVertical, stRotatedRight, stRotatedLeft),
		HFlipAndLeft(stHFlipAndLeft, stFlipVertical, stFlipHorizonal, stRotatedLeft, stRotatedRight);
	 */

	public BufferedImage rotate(BufferedImage inp, Orientation position) {
		switch (position) {
		case Original:
			return inp;
		case RotatedLeft:
			return rotateLeft(inp);
		case RotatedRight:
			return rotateRight(inp);
		case FlipVertical:
			return flipVertical(inp);
		case FlipHorizonal:
			return flipHorizontal(inp);
		case TurnedAbout:
			return turnAbout(inp);
		case HFlipAndRight:
			return hFlipAndRight(inp);
		case HFlipAndLeft:
			return hFlipAndLeft(inp);
		default:
			return null;
		}
	}

	public BufferedImage hFlipAndRight(BufferedImage inp) {
		return rotateRight(flipHorizontal(inp));
	}

	public BufferedImage hFlipAndLeft(BufferedImage inp) {
		return rotateLeft(flipHorizontal(inp));
	}

	public BufferedImage turnAbout(BufferedImage inp) {
		return flipVertical(flipHorizontal(inp));
	}

	public abstract BufferedImage flipVertical(BufferedImage inp);

	public abstract BufferedImage flipHorizontal(BufferedImage inp);

	public abstract BufferedImage rotateRight(BufferedImage inp);

	public abstract BufferedImage rotateLeft(BufferedImage inp);

}

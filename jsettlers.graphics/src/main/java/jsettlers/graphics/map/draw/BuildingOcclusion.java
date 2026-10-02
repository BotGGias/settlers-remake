/*******************************************************************************
 * Copyright (c) 2026
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.graphics.map.draw;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import go.graphics.ImageData;
import jsettlers.common.buildings.BuildingVariant;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.SingleImage;

/**
 * Decides whether a settler next to a building is drawn in front of or behind it.
 * <p>
 * A building is drawn as one image with the depth of its position, a settler with the depth of its row. At the sides of a building this is often wrong:
 * a settler in a row above the building position can stand in front of the building, and a settler in the same row right of the position is drawn in
 * front of it even if a part of the building covers him. The building image decides it: if the image covers the feet of the settler or lies below them,
 * he stands behind the building, otherwise in front of it.
 * <p>
 * Rows below the building position keep their depth: settlers there are in front of the building, and the image covering their feet is a walkable
 * ground part like a jetty.
 */
final class BuildingOcclusion {
	/**
	 * How many rows a settler is moved in front of or behind the building.
	 */
	static final float ROW_DISTANCE = .25f;

	private static final int SETTLER_HALF_WIDTH = 8;
	private static final int SETTLER_HEIGHT = 28;
	private static final int FOOT_HALF_WIDTH = 2;
	private static final int FOOT_HEIGHT = 3;
	private static final int MIN_OPAQUE_ALPHA = 0x80;

	private static final int IN_FRONT = 1;
	private static final int BEHIND = -1;

	private final int mapWidth;
	private final int mapHeight;

	/**
	 * Per building variant: triples of dx, dy and {@link #IN_FRONT} or {@link #BEHIND} for the positions whose depth needs a correction.
	 */
	private final Map<BuildingVariant, int[]> corrections = new HashMap<>();

	// open addressing hash table of the corrected positions of the current frame. An entry is used if its stamp is the current frame.
	private int frame = 1;
	private int size = 0;
	private int[] stamps = new int[1024];
	private int[] tileIndices = new int[1024];
	private float[] minRows = new float[1024];
	private float[] maxRows = new float[1024];

	BuildingOcclusion(int mapWidth, int mapHeight) {
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
	}

	/**
	 * Forgets the buildings of the last frame.
	 */
	void clear() {
		frame++;
		size = 0;
		if (frame == 0) {
			Arrays.fill(stamps, 0);
			frame = 1;
		}
	}

	/**
	 * Registers a building for the current frame.
	 *
	 * @param image
	 * 		Supplies the image of the finished building. It is only used the first time a building variant is registered.
	 */
	void addBuilding(int x, int y, BuildingVariant variant, Supplier<Image> image) {
		int[] variantCorrections = corrections.computeIfAbsent(variant, v -> computeCorrections(image.get()));

		for (int i = 0; i < variantCorrections.length; i += 3) {
			int tileX = x + variantCorrections[i];
			int tileY = y + variantCorrections[i + 1];
			if (tileX < 0 || tileY < 0 || tileX >= mapWidth || tileY >= mapHeight) {
				continue;
			}

			int slot = findOrCreateSlot(tileX + tileY * mapWidth);
			if (variantCorrections[i + 2] == IN_FRONT) {
				minRows[slot] = Math.max(minRows[slot], y + ROW_DISTANCE);
			} else {
				maxRows[slot] = Math.min(maxRows[slot], y - ROW_DISTANCE);
			}
		}
	}

	/**
	 * @param row
	 * 		The row that gives the depth of a settler standing at the given position. It lies between two rows while the settler walks.
	 * @return The row corrected for the buildings around the position.
	 */
	float correctRow(int x, int y, float row) {
		if (size == 0 || x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
			return row;
		}

		int slot = findSlot(x + y * mapWidth);
		if (slot < 0) {
			return row;
		}
		return Math.min(Math.max(row, minRows[slot]), maxRows[slot]);
	}

	private int findSlot(int tileIndex) {
		int mask = stamps.length - 1;
		for (int slot = hash(tileIndex) & mask; stamps[slot] == frame; slot = (slot + 1) & mask) {
			if (tileIndices[slot] == tileIndex) {
				return slot;
			}
		}
		return -1;
	}

	private int findOrCreateSlot(int tileIndex) {
		int slot = findSlot(tileIndex);
		if (slot >= 0) {
			return slot;
		}

		if (2 * (size + 1) > stamps.length) {
			grow();
		}

		int mask = stamps.length - 1;
		slot = hash(tileIndex) & mask;
		while (stamps[slot] == frame) {
			slot = (slot + 1) & mask;
		}
		stamps[slot] = frame;
		tileIndices[slot] = tileIndex;
		minRows[slot] = Float.NEGATIVE_INFINITY;
		maxRows[slot] = Float.POSITIVE_INFINITY;
		size++;
		return slot;
	}

	private void grow() {
		int[] oldStamps = stamps;
		int[] oldTileIndices = tileIndices;
		float[] oldMinRows = minRows;
		float[] oldMaxRows = maxRows;

		int capacity = 2 * oldStamps.length;
		stamps = new int[capacity];
		tileIndices = new int[capacity];
		minRows = new float[capacity];
		maxRows = new float[capacity];

		int mask = capacity - 1;
		for (int i = 0; i < oldStamps.length; i++) {
			if (oldStamps[i] == frame) {
				int slot = hash(oldTileIndices[i]) & mask;
				while (stamps[slot] == frame) {
					slot = (slot + 1) & mask;
				}
				stamps[slot] = frame;
				tileIndices[slot] = oldTileIndices[i];
				minRows[slot] = oldMinRows[i];
				maxRows[slot] = oldMaxRows[i];
			}
		}
	}

	private static int hash(int tileIndex) {
		return tileIndex * 0x9E3779B1 >>> 7;
	}

	/**
	 * Finds the positions around a building at which the row of a settler gives the wrong depth.
	 */
	static int[] computeCorrections(Image image) {
		if (!(image instanceof SingleImage) || image.getWidth() <= 0 || image.getHeight() <= 0) {
			return new int[0];
		}

		SingleImage singleImage = (SingleImage) image;
		ImageData data = singleImage.getData().convert(singleImage.getWidth(), singleImage.getHeight());
		OpaqueMask mask = new OpaqueMask(data, singleImage.getOffsetX(), singleImage.getOffsetY());

		int[] result = new int[48];
		int length = 0;
		int minDy = Math.floorDiv(mask.offsetY, DrawConstants.DISTANCE_Y);

		for (int dy = minDy; dy <= 0; dy++) {
			int rowShift = DrawConstants.DISTANCE_X / 2 * dy + mask.offsetX;
			int minDx = Math.floorDiv(rowShift - SETTLER_HALF_WIDTH, DrawConstants.DISTANCE_X);
			int maxDx = Math.floorDiv(rowShift + mask.width + SETTLER_HALF_WIDTH, DrawConstants.DISTANCE_X) + 1;
			for (int dx = minDx; dx <= maxDx; dx++) {
				if (dy == 0 && dx == 0) {
					continue;
				}

				// the settler is drawn with his feet at this point of the building image
				int footX = DrawConstants.DISTANCE_X * dx - DrawConstants.DISTANCE_X / 2 * dy - mask.offsetX;
				int footY = DrawConstants.DISTANCE_Y * dy - mask.offsetY;

				boolean drawnBehind = dy < 0 || dx < 0; // same row left of the position: drawn before the building with the same depth
				int correction;
				if (drawnBehind) {
					boolean overlaps = mask.countOpaque(footX - SETTLER_HALF_WIDTH, footY - SETTLER_HEIGHT, footX + SETTLER_HALF_WIDTH, footY) > 0;
					// building parts below the feet are in front of the settler, e.g. if he stands in a gap between two towers
					boolean partsInFront = mask.countOpaque(footX - FOOT_HALF_WIDTH, footY - FOOT_HEIGHT + 1, footX + FOOT_HALF_WIDTH, Integer.MAX_VALUE) > 0;
					correction = overlaps && !partsInFront ? IN_FRONT : 0;
				} else {
					correction = mask.coversFoot(footX, footY) ? BEHIND : 0;
				}

				if (correction != 0) {
					if (length + 3 > result.length) {
						result = Arrays.copyOf(result, 2 * result.length);
					}
					result[length++] = dx;
					result[length++] = dy;
					result[length++] = correction;
				}
			}
		}
		return Arrays.copyOf(result, length);
	}

	private static final class OpaqueMask {
		private final boolean[] opaque;
		private final int width;
		private final int height;
		private final int offsetX;
		private final int offsetY;

		OpaqueMask(ImageData data, int offsetX, int offsetY) {
			this.width = data.getWidth();
			this.height = data.getHeight();
			this.offsetX = offsetX;
			this.offsetY = offsetY;

			IntBuffer pixels = data.getReadData32();
			opaque = new boolean[width * height];
			for (int i = 0; i < opaque.length; i++) {
				opaque[i] = (pixels.get(i) & 0xff) >= MIN_OPAQUE_ALPHA; // RGBA
			}
		}

		boolean coversFoot(int footX, int footY) {
			int samples = (2 * FOOT_HALF_WIDTH + 1) * FOOT_HEIGHT;
			return 2 * countOpaque(footX - FOOT_HALF_WIDTH, footY - FOOT_HEIGHT + 1, footX + FOOT_HALF_WIDTH, footY) > samples;
		}

		int countOpaque(int minX, int minY, int maxX, int maxY) {
			int count = 0;
			for (int y = Math.max(minY, 0); y <= Math.min(maxY, height - 1); y++) {
				for (int x = Math.max(minX, 0); x <= Math.min(maxX, width - 1); x++) {
					if (opaque[x + y * width]) {
						count++;
					}
				}
			}
			return count;
		}
	}
}

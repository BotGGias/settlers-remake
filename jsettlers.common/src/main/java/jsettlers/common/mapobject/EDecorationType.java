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
package jsettlers.common.mapobject;

/**
 * Purely decorative landscape objects (stones, wrecks, plants, reefs, ...) as they are placed on original maps.
 * <p>
 * The sequence and image index reference the settler sequences of the objects graphics file (file 1). A blocking decoration blocks the tile
 * it is placed on.
 * <p>
 * The ordinal is used as state progress of the map object, so new values must only be appended.
 */
public enum EDecorationType {
	BIG_STONE_1(29, 0, true),
	BIG_STONE_2(29, 1, true),
	BIG_STONE_3(29, 2, true),
	BIG_STONE_4(29, 3, true),
	BIG_STONE_5(29, 4, true),
	BIG_STONE_6(29, 5, true),
	BIG_STONE_7(29, 6, true),
	BIG_STONE_8(29, 7, true),
	STONE_1(29, 8, true),
	STONE_2(29, 9, true),
	STONE_3(29, 10, true),
	STONE_4(29, 11, true),
	BOUNDARY_STONE_1(29, 12, true),
	BOUNDARY_STONE_2(29, 13, true),
	BOUNDARY_STONE_3(29, 14, true),
	BOUNDARY_STONE_4(29, 15, true),
	BOUNDARY_STONE_5(29, 16, true),
	BOUNDARY_STONE_6(29, 17, true),
	BOUNDARY_STONE_7(29, 18, true),
	BOUNDARY_STONE_8(29, 19, true),
	SMALL_STONE_1(30, 0, false),
	SMALL_STONE_2(30, 1, false),
	SMALL_STONE_3(30, 2, false),
	SMALL_STONE_4(30, 3, false),
	SMALL_STONE_5(30, 4, false),
	SMALL_STONE_6(30, 5, false),
	SMALL_STONE_7(30, 6, false),
	SMALL_STONE_8(30, 7, false),
	WRECK_1(28, 0, true),
	WRECK_2(28, 1, true),
	WRECK_3(28, 2, true),
	WRECK_4(28, 3, true),
	WRECK_5(28, 4, true),
	GRAVE(28, 5, false),
	PLANT_SMALL_1(27, 0, false),
	PLANT_SMALL_2(27, 1, false),
	PLANT_SMALL_3(27, 2, false),
	MUSHROOM_1(27, 3, false),
	MUSHROOM_2(27, 4, false),
	MUSHROOM_3(27, 5, false),
	TREE_STUMP_1(27, 6, false),
	TREE_STUMP_2(27, 7, false),
	TREE_DEAD_1(27, 8, false),
	TREE_DEAD_2(27, 9, false),
	CACTUS_1(27, 10, false),
	CACTUS_2(27, 11, false),
	CACTUS_3(27, 12, false),
	CACTUS_4(27, 13, false),
	BONES(27, 14, false),
	FLOWER_1(27, 15, false),
	FLOWER_2(27, 16, false),
	FLOWER_3(27, 17, false),
	SHRUB_SMALL_1(27, 18, false),
	SHRUB_SMALL_2(27, 19, false),
	SHRUB_SMALL_3(27, 20, false),
	SHRUB_SMALL_4(27, 21, false),
	SHRUB_1(27, 22, false),
	SHRUB_2(27, 23, false),
	SHRUB_3(27, 24, false),
	SHRUB_4(27, 25, false),
	SHRUB_5(27, 26, false),
	REED_BEDS_1(27, 27, false),
	REED_BEDS_2(27, 28, false),
	REED_BEDS_3(27, 29, false),
	REED_BEDS_4(27, 30, false),
	REED_BEDS_5(27, 31, false),
	REED_BEDS_6(27, 32, false),
	REEF_SMALL(29, 20, false),
	REEF_MEDIUM(29, 21, false),
	REEF_LARGE(29, 22, false),
	REEF_XLARGE(29, 23, false);

	public static final EDecorationType[] VALUES = EDecorationType.values();

	/**
	 * The settler sequence in the objects graphics file.
	 */
	public final int sequence;
	/**
	 * The image index inside of {@link #sequence}.
	 */
	public final int imageIndex;
	/**
	 * If true, the decoration blocks the tile it is placed on.
	 */
	public final boolean blocking;

	EDecorationType(int sequence, int imageIndex, boolean blocking) {
		this.sequence = sequence;
		this.imageIndex = imageIndex;
		this.blocking = blocking;
	}
}

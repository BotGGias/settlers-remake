/*******************************************************************************
 * Copyright (c) 2015 - 2017
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
package jsettlers.logic.map.loading.original.data;

import jsettlers.common.mapobject.EDecorationType;
import jsettlers.logic.map.loading.data.objects.DecorationMapDataObject;
import jsettlers.logic.map.loading.data.objects.MapDataObject;
import jsettlers.logic.map.loading.data.objects.MapTreeObject;
import jsettlers.logic.map.loading.data.objects.StoneMapDataObject;

/**
 * The map objects on the map, see {@link EOriginalMapObjectClass}
 * @author Thomas Zeugner
 * @author codingberlin
 */
public enum EOriginalMapObjectType {

	NO_OBJECT(null, 0), // - 0

	BIG_STONE_1(EDecorationType.BIG_STONE_1), // - GAME_OBJECT_BIG_STONE_1 = 1,
	BIG_STONE_2(EDecorationType.BIG_STONE_2), // - GAME_OBJECT_BIG_STONE_2 = 2,
	BIG_STONE_3(EDecorationType.BIG_STONE_3), // - GAME_OBJECT_BIG_STONE_3 = 3,
	BIG_STONE_4(EDecorationType.BIG_STONE_4), // - GAME_OBJECT_BIG_STONE_4 = 4,
	BIG_STONE_5(EDecorationType.BIG_STONE_5), // - GAME_OBJECT_BIG_STONE_5 = 5,
	BIG_STONE_6(EDecorationType.BIG_STONE_6), // - GAME_OBJECT_BIG_STONE_6 = 6,
	BIG_STONE_7(EDecorationType.BIG_STONE_7), // - GAME_OBJECT_BIG_STONE_7 = 7,
	BIG_STONE_8(EDecorationType.BIG_STONE_8), // - GAME_OBJECT_BIG_STONE_8 = 8,
	STONE_1(EDecorationType.STONE_1), // - GAME_OBJECT_STONE_1 = 9,
	STONE_2(EDecorationType.STONE_2), // - GAME_OBJECT_STONE_2 = 10,
	STONE_3(EDecorationType.STONE_3), // - GAME_OBJECT_STONE_3 = 11,
	STONE_4(EDecorationType.STONE_4), // - GAME_OBJECT_STONE_4 = 12,
	BOUNDARY_STONE_1(EDecorationType.BOUNDARY_STONE_1), // - GAME_OBJECT_BOUNDERY_STONE_1 = 13,
	BOUNDARY_STONE_2(EDecorationType.BOUNDARY_STONE_2), // - GAME_OBJECT_BOUNDERY_STONE_2 = 14,
	BOUNDARY_STONE_3(EDecorationType.BOUNDARY_STONE_3), // - GAME_OBJECT_BOUNDERY_STONE_3 = 15,
	BOUNDARY_STONE_4(EDecorationType.BOUNDARY_STONE_4), // - GAME_OBJECT_BOUNDERY_STONE_4 = 16,
	BOUNDARY_STONE_5(EDecorationType.BOUNDARY_STONE_5), // - GAME_OBJECT_BOUNDERY_STONE_5 = 17,
	BOUNDARY_STONE_6(EDecorationType.BOUNDARY_STONE_6), // - GAME_OBJECT_BOUNDERY_STONE_6 = 18,
	BOUNDARY_STONE_7(EDecorationType.BOUNDARY_STONE_7), // - GAME_OBJECT_BOUNDERY_STONE_7 = 19,
	BOUNDARY_STONE_8(EDecorationType.BOUNDARY_STONE_8), // - GAME_OBJECT_BOUNDERY_STONE_8 = 20,
	SMALL_STONE_1(EDecorationType.SMALL_STONE_1), // - GAME_OBJECT_SMALL_STONE_1 = 21,
	SMALL_STONE_2(EDecorationType.SMALL_STONE_2), // - GAME_OBJECT_SMALL_STONE_2 = 22,
	SMALL_STONE_3(EDecorationType.SMALL_STONE_3), // - GAME_OBJECT_SMALL_STONE_3 = 23,
	SMALL_STONE_4(EDecorationType.SMALL_STONE_4), // - GAME_OBJECT_SMALL_STONE_4 = 24,
	SMALL_STONE_5(EDecorationType.SMALL_STONE_5), // - GAME_OBJECT_SMALL_STONE_5 = 25,
	SMALL_STONE_6(EDecorationType.SMALL_STONE_6), // - GAME_OBJECT_SMALL_STONE_6 = 26,
	SMALL_STONE_7(EDecorationType.SMALL_STONE_7), // - GAME_OBJECT_SMALL_STONE_7 = 27,
	SMALL_STONE_8(EDecorationType.SMALL_STONE_8), // - GAME_OBJECT_SMALL_STONE_8 = 28,
	WRECK_1(EDecorationType.WRECK_1), // - GAME_OBJECT_WRECK_1 = 29,
	WRECK_2(EDecorationType.WRECK_2), // - GAME_OBJECT_WRECK_2 = 30,
	WRECK_3(EDecorationType.WRECK_3), // - GAME_OBJECT_WRECK_3 = 31,
	WRECK_4(EDecorationType.WRECK_4), // - GAME_OBJECT_WRECK_4 = 32,
	WRECK_5(EDecorationType.WRECK_5), // - GAME_OBJECT_WRECK_5 = 33,
	GRAVE(EDecorationType.GRAVE), // - GAME_OBJECT_GRAVE = 34,
	PLANT_SMALL_1(EDecorationType.PLANT_SMALL_1), // - GAME_OBJECT_PLANT_SMALL_1 = 35,
	PLANT_SMALL_2(EDecorationType.PLANT_SMALL_2), // - GAME_OBJECT_PLANT_SMALL_2 = 36,
	PLANT_SMALL_3(EDecorationType.PLANT_SMALL_3), // - GAME_OBJECT_PLANT_SMALL_3 = 37,
	MUSHROOM_1(EDecorationType.MUSHROOM_1), // - GAME_OBJECT_MUSHROOM_1 = 38,
	MUSHROOM_2(EDecorationType.MUSHROOM_2), // - GAME_OBJECT_MUSHROOM_2 = 39,
	MUSHROOM_3(EDecorationType.MUSHROOM_3), // - GAME_OBJECT_MUSHROOM_3 = 40,
	TREE_STUMP_1(EDecorationType.TREE_STUMP_1), // - GAME_OBJECT_TREE_STUMP_1 = 41,
	TREE_STUMP_2(EDecorationType.TREE_STUMP_2), // - GAME_OBJECT_TREE_STUMP_2 = 42,
	TREE_DEAD_1(EDecorationType.TREE_DEAD_1), // - GAME_OBJECT_TREE_DEAD_1 = 43,
	TREE_DEAD_2(EDecorationType.TREE_DEAD_2), // - GAME_OBJECT_TREE_DEAD_2 = 44,
	CACTUS_1(EDecorationType.CACTUS_1), // - GAME_OBJECT_CACTUS_1 = 45,
	CACTUS_2(EDecorationType.CACTUS_2), // - GAME_OBJECT_CACTUS_2 = 46,
	CACTUS_3(EDecorationType.CACTUS_3), // - GAME_OBJECT_CACTUS_3 = 47,
	CACTUS_4(EDecorationType.CACTUS_4), // - GAME_OBJECT_CACTUS_4 = 48,
	BONES(EDecorationType.BONES), // - GAME_OBJECT_BONES = 49,
	FLOWER_1(EDecorationType.FLOWER_1), // - GAME_OBJECT_FLOWER_1 = 50,
	FLOWER_2(EDecorationType.FLOWER_2), // - GAME_OBJECT_FLOWER_2 = 51,
	FLOWER_3(EDecorationType.FLOWER_3), // - GAME_OBJECT_FLOWER_3 = 52,
	SHRUB_SMALL_1(EDecorationType.SHRUB_SMALL_1), // - GAME_OBJECT_STRUB_SMALL_1 = 53,
	SHRUB_SMALL_2(EDecorationType.SHRUB_SMALL_2), // - GAME_OBJECT_STRUB_SMALL_2 = 54,
	SHRUB_SMALL_3(EDecorationType.SHRUB_SMALL_3), // - GAME_OBJECT_STRUB_SMALL_3 = 55,
	SHRUB_SMALL_4(EDecorationType.SHRUB_SMALL_4), // - GAME_OBJECT_STRUB_SMALL_4 = 56,
	SHRUB_1(EDecorationType.SHRUB_1), // - GAME_OBJECT_STRUB_1 = 57,
	SHRUB_2(EDecorationType.SHRUB_2), // - GAME_OBJECT_STRUB_2 = 58,
	SHRUB_3(EDecorationType.SHRUB_3), // - GAME_OBJECT_STRUB_3 = 59,
	SHRUB_4(EDecorationType.SHRUB_4), // - GAME_OBJECT_STRUB_4 = 60,
	SHRUB_5(EDecorationType.SHRUB_5), // - GAME_OBJECT_STRUB_5 = 61,
	REED_BEDS_1(EDecorationType.REED_BEDS_1), // - GAME_OBJECT_REED_BEDS_1 = 62,
	REED_BEDS_2(EDecorationType.REED_BEDS_2), // - GAME_OBJECT_REED_BEDS_2 = 63,
	REED_BEDS_3(EDecorationType.REED_BEDS_3), // - GAME_OBJECT_REED_BEDS_3 = 64,
	REED_BEDS_4(EDecorationType.REED_BEDS_4), // - GAME_OBJECT_REED_BEDS_4 = 65,
	REED_BEDS_5(EDecorationType.REED_BEDS_5), // - GAME_OBJECT_REED_BEDS_5 = 66,
	REED_BEDS_6(EDecorationType.REED_BEDS_6), // - GAME_OBJECT_REED_BEDS_6 = 67,
	TREE_BIRCH_1(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_BIRCH_1 = 68,
	TREE_BIRCH_2(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_BIRCH_2 = 69,
	TREE_ELM_1(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_ELM_1 = 70,
	TREE_ELM_2(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_ELM_2 = 71,
	TREE_OAK_1(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_OAK_1 = 72,
	TREE_UNKNOWN_1(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_UNKNOWN_1 = 73,
	TREE_UNKNOWN_2(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_UNKNOWN_2 = 74,
	TREE_UNKNOWN_3(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_UNKNOWN_3 = 75,
	TREE_UNKNOWN_4(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_UNKNOWN_4 = 76,
	TREE_UNKNOWN_5(EOriginalMapObjectClass.TREE, 0), // - //-- unknown: 77
	TREE_ARECACEAE_1(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_ARECACEAE_1 = 78,
	TREE_ARECACEAE_2(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_ARECACEAE_2 = 79,
	TREE_UNKNOWN_6(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_UNKNOWN_6 = 80,
	UNKNOWN_51(null, 0), // - //-- unknown: 81
	UNKNOWN_52(null, 0), // - //-- unknown: 82
	UNKNOWN_53(null, 0), // - //-- unknown: 83
	UNKNOWN_54(EOriginalMapObjectClass.TREE, 0), // - GAME_OBJECT_TREE_SMALL = 84,
	UNKNOWN_55(null, 0), // - //-- unknown...
	UNKNOWN_56(null, 0), // - //-- unknown...
	UNKNOWN_57(null, 0), // - //-- unknown...
	UNKNOWN_58(null, 0), // - //-- unknown...
	UNKNOWN_59(null, 0), // - //-- unknown...
	UNKNOWN_5A(null, 0), // - //-- unknown...
	UNKNOWN_5B(null, 0), // - //-- unknown...
	UNKNOWN_5C(null, 0), // - //-- unknown...
	UNKNOWN_5D(null, 0), // - //-- unknown...
	UNKNOWN_5E(null, 0), // - //-- unknown...
	UNKNOWN_5F(null, 0), // - //-- unknown...
	UNKNOWN_60(null, 0), // - //-- unknown...
	UNKNOWN_61(null, 0), // - //-- unknown...
	UNKNOWN_62(null, 0), // - //-- unknown...
	UNKNOWN_63(null, 0), // - //-- unknown...
	UNKNOWN_64(null, 0), // - //-- unknown...
	UNKNOWN_65(null, 0), // - //-- unknown...
	UNKNOWN_66(null, 0), // - //-- unknown...
	UNKNOWN_67(null, 0), // - //-- unknown...
	UNKNOWN_68(null, 0), // - //-- unknown...
	UNKNOWN_69(null, 0), // - //-- unknown...
	UNKNOWN_6A(null, 0), // - //-- unknown...
	UNKNOWN_6B(null, 0), // - //-- unknown...
	UNKNOWN_6C(null, 0), // - //-- unknown...
	UNKNOWN_6D(null, 0), // - //-- unknown...
	UNKNOWN_6E(null, 0), // - //-- unknown...
	REEF_SMALL(EDecorationType.REEF_SMALL), // - GAME_OBJECT_REEF_SMALL = 111,
	REEF_MEDIUM(EDecorationType.REEF_MEDIUM), // - GAME_OBJECT_REEF_MEDIUM = 112,
	REEF_LARGE(EDecorationType.REEF_LARGE), // - GAME_OBJECT_REEF_LARGE = 113,
	REEF_XLARGE(EDecorationType.REEF_XLARGE), // - GAME_OBJECT_REEF_XLARGE = 114,
	RES_STONE_01(EOriginalMapObjectClass.STONE, 12), // - GAME_OBJECT_RES_STONE_01 = 115,
	RES_STONE_02(EOriginalMapObjectClass.STONE, 11), // - GAME_OBJECT_RES_STONE_02 = 116,
	RES_STONE_03(EOriginalMapObjectClass.STONE, 10), // - GAME_OBJECT_RES_STONE_03 = 117,
	RES_STONE_04(EOriginalMapObjectClass.STONE, 9), // - GAME_OBJECT_RES_STONE_04 = 118,
	RES_STONE_05(EOriginalMapObjectClass.STONE, 8), // - GAME_OBJECT_RES_STONE_05 = 119,
	RES_STONE_06(EOriginalMapObjectClass.STONE, 7), // - GAME_OBJECT_RES_STONE_06 = 120,
	RES_STONE_07(EOriginalMapObjectClass.STONE, 6), // - GAME_OBJECT_RES_STONE_07 = 121,
	RES_STONE_08(EOriginalMapObjectClass.STONE, 5), // - GAME_OBJECT_RES_STONE_08 = 122,
	RES_STONE_09(EOriginalMapObjectClass.STONE, 4), // - GAME_OBJECT_RES_STONE_09 = 123,
	RES_STONE_10(EOriginalMapObjectClass.STONE, 3), // - GAME_OBJECT_RES_STONE_10 = 124,
	RES_STONE_11(EOriginalMapObjectClass.STONE, 2), // - GAME_OBJECT_RES_STONE_11 = 125,
	RES_STONE_12(EOriginalMapObjectClass.STONE, 1), // - GAME_OBJECT_RES_STONE_12 = 126,
	RES_STONE_13(EOriginalMapObjectClass.STONE, 0); // - GAME_OBJECT_RES_STONE_13 = 127,;
	
	private static final EOriginalMapObjectType[] VALUES = EOriginalMapObjectType.values();

	public final EOriginalMapObjectClass type;
	public final int style;
	public final EDecorationType decoration;

	EOriginalMapObjectType(EOriginalMapObjectClass type, int style) {
		this.type = type;
		this.style = style;
		this.decoration = null;
	}

	EOriginalMapObjectType(EDecorationType decoration) {
		this.type = EOriginalMapObjectClass.DECORATION;
		this.style = 0;
		this.decoration = decoration;
	}

	public static EOriginalMapObjectType getTypeByInt(int type) {
		if (type < 0 || type >= VALUES.length) {
			return NO_OBJECT;
		} else {
			return VALUES[type];
		}
	}

	public MapDataObject getNewInstance() {
		if (type == null) {
			return null;
		}

		switch (type) {
		case DECORATION:
			return new DecorationMapDataObject(decoration);

		case STONE:
			return StoneMapDataObject.getInstance(style);

		case TREE:
			return MapTreeObject.getInstance();

		default:
			return null;
		}
	}

}
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
package jsettlers.logic.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.player.ECivilisation;

public class ProductionStatisticTest {

	@Test
	public void testCountsProducedMaterials() {
		ProductionStatistic statistic = new ProductionStatistic();
		statistic.materialProduced(EMaterialType.PLANK);
		statistic.materialProduced(EMaterialType.PLANK);
		statistic.materialProduced(EMaterialType.GOLD);

		assertEquals(2, statistic.getAmountProduced(EMaterialType.PLANK));
		assertEquals(1, statistic.getAmountProduced(EMaterialType.GOLD));
		assertEquals(0, statistic.getAmountProduced(EMaterialType.STONE));
	}

	@Test
	public void testIgnoresNonDroppableMaterials() {
		ProductionStatistic statistic = new ProductionStatistic();
		statistic.materialProduced(null);
		statistic.materialProduced(EMaterialType.NO_MATERIAL);
		statistic.materialProduced(EMaterialType.TREE);

		assertEquals(0, statistic.getAmountProduced(EMaterialType.NO_MATERIAL));
		assertEquals(0, statistic.getAmountProduced(EMaterialType.TREE));
		assertEquals(0, statistic.getAmountProduced(null));
	}

	@Test
	public void testPlayerSerialization() throws IOException, ClassNotFoundException {
		Player player = new Player((byte) 0, new Team((byte) 0), (byte) 1, EPlayerType.HUMAN, ECivilisation.ASIAN);
		player.getProductionStatistic().materialProduced(EMaterialType.RICE);
		player.getProductionStatistic().materialProduced(EMaterialType.RICE);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
			oos.writeObject(player);
		}
		Player read;
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			read = (Player) ois.readObject();
		}

		assertNotNull(read.getProductionStatistic());
		assertEquals(2, read.getProductionStatistic().getAmountProduced(EMaterialType.RICE));
	}
}

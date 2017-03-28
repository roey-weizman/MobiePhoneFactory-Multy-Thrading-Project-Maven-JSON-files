/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;

/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
	/**
	 * Begin the simulation Should not be called before
	 * attachWorkStealingThreadPool()
	 */
	private static WaveTask[] Waves;
	private static WorkStealingThreadPool myPool;
	/* protected */ static ConcurrentLinkedQueue<Product> ans = new ConcurrentLinkedQueue<Product>();
	static int numOfT;

	static ConcurrentLinkedQueue<Product> getAns() {
		return ans;
	}

	public static ConcurrentLinkedQueue<Product> start() {
		CountDownLatch l = new CountDownLatch(Waves.length - 1);
		for (int g = 0; g < Waves.length; g++) {
			myPool.submit(Waves[g]);
			Waves[g].getResult().whenResolved(() -> {
				l.countDown();
			});
			myPool.start();

			try {
				l.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.currentThread();
				Thread.sleep(150);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				myPool.shutdown();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.currentThread();
				Thread.sleep(150);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myPool = new WorkStealingThreadPool(numOfT);
		}


		return ans;
	}

	/**
	 * attach a WorkStealingThreadPool to the Simulator, this
	 * WorkStealingThreadPool will be used to run the simulation
	 * 
	 * @param myWorkStealingThreadPool
	 *            - the WorkStealingThreadPool which will be used by the
	 *            simulator
	 */
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool) {
		myPool = myWorkStealingThreadPool;
	}

	public static void main(String[] args) {
		Warehouse w = new Warehouse();
		try {

			JsonObject jsonObject = recieveInputFile(args);
			readTools(jsonObject, w);
			JsonArray arrayOfPlans = (JsonArray) jsonObject.get("plans");
			readPlans(arrayOfPlans, jsonObject, w);
			readWaves(arrayOfPlans, jsonObject, w);

		} catch (IOException e) {
			e.printStackTrace();
		}
		startAndCreateOutputFile();

	}

	private static String[] jsonArrayToStringArray(JsonArray jsonArray) {
		int arraySize = jsonArray.size();
		String[] stringArray = new String[arraySize];

		for (int i = 0; i < arraySize; i++) {
			stringArray[i] = jsonArray.get(i).getAsString();
		}

		return stringArray;
	};

	private static Tool createToolFromString(String s) {
		if (s.equals("rs-pliers"))
			return new RandomSumPliers();
		else if (s.equals("np-hammer"))
			return new NextPrimeHammer();
		else
			return new GcdScrewDriver();
	}

	static void readTools(JsonObject jsonObject, Warehouse w) {
		int numOfThreads = jsonObject.get("threads").getAsInt();
		numOfT = numOfThreads;
		attachWorkStealingThreadPool(new WorkStealingThreadPool(numOfThreads));
		JsonArray arrayOfTools = (JsonArray) jsonObject.get("tools");
		Iterator<JsonElement> iterator = arrayOfTools.iterator();
		int i = 0;
		while (iterator.hasNext() && i < 3) {
			String toolName = arrayOfTools.get(i).getAsJsonObject().get("tool").getAsString();
			Tool t = createToolFromString(toolName);
			int amountOfTool = arrayOfTools.get(i).getAsJsonObject().get("qty").getAsInt();
			w.addTool(t, amountOfTool);
			i++;
		}
	}

	static void readPlans(JsonArray arrayOfPlans, JsonObject jsonObject, Warehouse w) {

		Iterator<JsonElement> iterator1 = arrayOfPlans.iterator();
		int j = 0;
		while (iterator1.hasNext() && j < arrayOfPlans.size()) {

			String myProduct = arrayOfPlans.get(j).getAsJsonObject().get("product").getAsString();
			String[] myParts = jsonArrayToStringArray(
					arrayOfPlans.get(j).getAsJsonObject().get("parts").getAsJsonArray());
			String[] myTools = jsonArrayToStringArray(
					arrayOfPlans.get(j).getAsJsonObject().get("tools").getAsJsonArray());
			ManufactoringPlan m = new ManufactoringPlan(myProduct, myParts, myTools);
			w.addPlan(m);
			iterator1.next();
			j++;
		}
	}

	static void readWaves(JsonArray arrayOfPlans, JsonObject jsonObject, Warehouse w) {
		JsonArray arrayOfWaves = (JsonArray) jsonObject.get("waves");
		Iterator<JsonElement> iterator2 = arrayOfPlans.iterator();
		int k = 0;
		Waves = new WaveTask[arrayOfWaves.size()];
		while (iterator2.hasNext() && k < arrayOfWaves.size()) {

			JsonArray currArray = arrayOfWaves.get(k).getAsJsonArray();
			Iterator<JsonElement> iterator3 = arrayOfPlans.iterator();
			int x = 0;
			while (iterator3.hasNext() && x < currArray.size()) {
				Object[] CurrWave = new Object[currArray.size() * 3];
				for (int u = 0; u < CurrWave.length; u = u + 3) {
					CurrWave[u] = currArray.get(x).getAsJsonObject().get("product").getAsString();
					CurrWave[u + 1] = currArray.get(x).getAsJsonObject().get("qty").getAsInt();
					CurrWave[u + 2] = currArray.get(x).getAsJsonObject().get("startId").getAsLong();
					iterator3.next();
					x++;
				}
				WaveTask newWave = new WaveTask(CurrWave, w);
				Waves[k] = newWave;
			}
			iterator2.next();
			k++;

		}
	}

	static void startAndCreateOutputFile() {
		ConcurrentLinkedQueue<Product> SimulationResult;
		SimulationResult = start();
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream("result.ser");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(fout);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			oos.writeObject(SimulationResult);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static JsonObject recieveInputFile(String[] args)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonParser parser = new JsonParser();
		String path = args[0];
		Object obj = parser.parse(new FileReader(path));
		JsonObject jsonObject = (JsonObject) obj;
		return jsonObject;
	}
}

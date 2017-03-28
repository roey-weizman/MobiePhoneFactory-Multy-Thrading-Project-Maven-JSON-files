/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.test;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MergeSort extends Task<int[]> {

	private final int[] array;

	public MergeSort(int[] array) {
		this.array = array;
	}

	@Override
	protected void start() {
		if(array.length==1)
			complete(array);
		else{
			ArrayList<Task<int[]>> Mytasks = new ArrayList<>();
			int[] array1=new int[array.length/2];
			int[] array2=new int[(array.length/2)+(array.length%2)];
			for(int i=0;i<array1.length;i++){
				array1[i]=array[i];
			}
			for(int i=0;i<array2.length;i++){
				array2[i]=array[i+(array.length/2)];
			}
			MergeSort Part1=new MergeSort(array1);
			MergeSort Part2=new MergeSort(array2);
			Mytasks.add(Part1);
			Mytasks.add(Part2);
			spawn(Part1,Part2);
			whenResolved(Mytasks,()->{
				int Mysize=Mytasks.get(0).getResult().get().length;
				Mysize+=Mytasks.get(1).getResult().get().length;
				int[] toBeSort=new int[array.length] ;//= new int[];
				int counter1 = 0, counter2 = 0, index= 0;
				while (counter1 <Mytasks.get(0).getResult().get().length & counter2 < Mytasks.get(1).getResult().get().length){
					if (Mytasks.get(0).getResult().get()[counter1 ]<Mytasks.get(1).getResult().get()[counter2]){
						toBeSort[index] = Mytasks.get(0).getResult().get()[counter1 ];
						counter1 ++;
					}
					else{
						toBeSort[index] = Mytasks.get(1).getResult().get()[counter2];
						counter2++;
					}
					index++;
				}
				for (int i = counter1 ; i < Mytasks.get(0).getResult().get().length; i++){
					toBeSort[index] = Mytasks.get(0).getResult().get()[i];
					index++;
				}
				for (int j = counter2; j < Mytasks.get(1).getResult().get().length ; j++){
					toBeSort[index] = Mytasks.get(1).getResult().get()[j];
					index++;
				}
				
				try{complete(toBeSort);}
				catch(IllegalStateException e){}
			}
					);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
		int n = 100000; //you may check on different number of elements if you like
		int[] array = new Random().ints(n).toArray();

		MergeSort task = new MergeSort(array);

		CountDownLatch l = new CountDownLatch(1);
		pool.start();
		pool.submit(task);
		task.getResult().whenResolved(() -> {
			//warning - a large print!! - you can remove this line if you wish
			System.out.println(Arrays.toString(task.getResult().get()));
			System.out.println();
			l.countDown();
		});

		try {
			l.await();
		} catch (InterruptedException e) {
		}
		pool.shutdown();
	
	}
}
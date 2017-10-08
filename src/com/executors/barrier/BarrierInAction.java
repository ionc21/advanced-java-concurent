package com.executors.barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BarrierInAction {

	public static void main(String[] args) {

		class Friend implements Callable<String> {

			private CyclicBarrier barrier;

			public Friend(CyclicBarrier barrier) {
				this.barrier = barrier;
			}

			@Override
			public String call() throws Exception {

				try {
					Random random = new Random();
					Thread.sleep((random.nextInt(20) * 100 + 100));
					System.out.println("I just arrived, waiting for the others...");

					barrier.await();

					System.out.println("Let's go to the cinema!");
					return "ok";
				} catch (InterruptedException e) {
					System.out.println("Interrupted");
				}
				return "nok";
			}
		}

		// it will throw TimeoutException and after BrokenBarrierException if not enough
		// will be submitted
		// ExecutorService executorService = Executors.newFixedThreadPool(2);
		ExecutorService executorService = Executors.newFixedThreadPool(4);

		CyclicBarrier barrier = new CyclicBarrier(4, () -> System.out.println("Barrier openning"));
		List<Future<String>> futures = new ArrayList<>();

		try {
			for (int i = 0; i < 4; i++) {
				Friend friend = new Friend(barrier);
				futures.add(executorService.submit(friend));
			}

			futures.forEach(future -> {
				try {
					// future.get(1000, TimeUnit.MILLISECONDS);
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					System.out.println(e.getMessage());
				}
				// catch (TimeoutException e) {
				// System.out.println("Timed out");
				// future.cancel(true);
				// }
			});

		} finally {
			executorService.shutdown();
		}
	}

}

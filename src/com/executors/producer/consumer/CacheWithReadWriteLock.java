package com.executors.producer.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheWithReadWriteLock {

	private Map<Long, String> cache = new HashMap<>();
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock readLock = lock.readLock();
	private Lock writeLock = lock.writeLock();

	public String put(Long key, String value) {
		writeLock.lock();
		try {
			return cache.put(key, value);
		} finally {
			writeLock.unlock();
		}
	}

	public String get(Long key) {
		readLock.lock();
		try {
			return cache.get(key);
		} finally {
			readLock.unlock();
		}
	}

	public Map<Long, String> getCache() {
		return cache;
	}

	public static void main(String[] args) {

		CacheWithReadWriteLock readWriteCache = new CacheWithReadWriteLock();

		class Producer implements Callable<String> {

			private Random rand = new Random();

			@Override
			public String call() throws Exception {
				long key = rand.nextInt(1_000);
				while (key < 900) {
					readWriteCache.put(key, Long.toString(key));
					if (readWriteCache.get(key) == null) {
						System.out.println("Key " + key + " has not been put in the map");
					}
				}
				return "All elements added to the list";
			}
		}

		ExecutorService executorService = Executors.newFixedThreadPool(4);

		System.out.println("Adding value...");

		try {
			for (int i = 0; i < 4; i++) {
				executorService.submit(new Producer());
			}
		} finally {
			executorService.shutdown();
		}

	}
}

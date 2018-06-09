package com.chocohead.spipes.logic;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;

public class RenderPool implements Runnable {
	private static final BlockingQueue<RunnableFuture<?>> tasks = new LinkedBlockingQueue<>();

	public static <T> RunnableFuture<T> queue(Callable<T> task) {
		/*if (!tasks.offer(new FutureTask<>(task))) {
			System.out.println("Slow task in render pool, skipping!");

			try {
				Future<?> slow = tasks.take();
				slow.cancel(true);
			} catch (InterruptedException e) {
				throw new RuntimeException("Bad", e);
			}

			queue(task);
		}*/
		RunnableFuture<T> out = new FutureTask<>(task);
		tasks.add(out);
		return out;
	}

	@Override
	public void run() {
		try {
			while (true) {
				RunnableFuture<?> task = tasks.take();

				task.run();
			}
		} catch (InterruptedException e) {
			System.out.println("Render pool interupted");
			e.printStackTrace();
			//Probably time to stop
		}
	}
}
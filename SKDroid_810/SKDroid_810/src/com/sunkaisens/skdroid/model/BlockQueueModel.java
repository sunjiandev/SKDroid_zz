package com.sunkaisens.skdroid.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockQueueModel {

	private BlockingQueue<String> sendInstantQueue = new ArrayBlockingQueue<String>(
			1000);

	public void clearQueue() {
		sendInstantQueue.clear();

	}

	public void putUser(String userNo) {
		sendInstantQueue.offer(userNo);
	}

	public String getUser() throws InterruptedException {
		return sendInstantQueue.take();
	}

	public String getMessageSend() {
		return sendInstantQueue.poll();
	}

}

package projects.defaultProject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import sinalgo.tools.Tuple;

public class RequestQueue {

	private String separator = ",";

	private int numberOfNodes;
	private int numberOfRequests;
	private Queue<String> queue = new LinkedList<>();

	public RequestQueue() {
		this.numberOfNodes = 0;
		this.numberOfRequests = 0;
	}

	public RequestQueue(String path) {
		this.setDataFromFile(path);
	}

	public RequestQueue(String path, String separator) {
		this.separator = separator;
		this.setDataFromFile(path);
	}

	public void setDataFromFile(String path) {
		queue.clear();

		BufferedReader reader;

		try {

			String line;
			reader = new BufferedReader(new FileReader(path));

			if ((line = reader.readLine()) != null) {
				String[] fields = line.split(this.separator);
				this.numberOfNodes = Integer.parseInt(fields[0]);
				this.numberOfRequests = Integer.parseInt(fields[1]);
			}

			while ((line = reader.readLine()) != null) {
				queue.add(line);
			}

			reader.close();

		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}

	}

	public int getNumberOfNodes() {
		return this.numberOfNodes;
	}

	public int getNumberOfRequests() {
		return this.numberOfRequests;
	}

	public Tuple<Integer, Integer> getNextRequest() {
		String line = this.queue.poll();
		String[] fields = line.split(this.separator);
		Tuple<Integer, Integer> r = new Tuple<>(Integer.valueOf(fields[0]), Integer.valueOf(fields[1]));
		return r;
	}

	public boolean hasNextRequest() {
		return !this.queue.isEmpty();
	}

}
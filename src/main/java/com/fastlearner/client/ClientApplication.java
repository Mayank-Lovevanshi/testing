package com.fastlearner.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
class Product
{
	private String name;
	private String category;
	private Integer price;
	public Product(String name,Integer price,String category)
	{
		this.name = name;
		this.price = price;
		this.category = category;
	}
}

@SpringBootApplication
public class ClientApplication {
	private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);
	public static void main(String[] args) throws InterruptedException {
		ConcurrentRequest[] concurrentRequests = new ConcurrentRequest[100];
		int start = 130501;
		for(int i=0;i<concurrentRequests.length;i++){
			concurrentRequests[i] = new ConcurrentRequest(start,start+8694);
			start+=8695;
		}
		for(int i=0;i<concurrentRequests.length;i++) {
			concurrentRequests[i].start();
		}
		for(int i=0;i<concurrentRequests.length;i++) {
			concurrentRequests[i].join();
		}

	}
}

class ConcurrentRequest extends Thread {
	private final int start;
	private final int end;
	public ConcurrentRequest(int start, int end) {
		this.start = start;
		this.end = end;
	}
	private final Logger log = LoggerFactory.getLogger(ConcurrentRequest.class);
	public void run() {
		String[] category = {"Electronics", "Health", "Beauty", "Gym"};
		ObjectMapper mapper = new ObjectMapper();
		HttpClient client = HttpClient.newHttpClient();
		long maxTime = 0;
		for (int i = start; i <= end; i++) {
			Product product = new Product("Product " + i, (i % 187) + 24, category[i % 4]);
			long startTime = System.currentTimeMillis();
			try {
				String jsonBody = mapper.writeValueAsString(product);
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create("http://localhost:8080/api/v1/products"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
						.build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				long duration = System.currentTimeMillis() - startTime;
				maxTime = Math.max(maxTime, duration);
				if (response.statusCode() >= 200 && response.statusCode() <= 300) {
					log.info("{}ms | {} | SUCCESS", duration, product.getName());
				}
			} catch (Exception e) {
				log.error("{} | {} | FAILED", System.currentTimeMillis(), product.getName());
			}
		}
		System.out.println("Maximum Time taken : " + maxTime+"ms");
	}
}


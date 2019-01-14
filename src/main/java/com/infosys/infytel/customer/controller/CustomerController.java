package com.infosys.infytel.customer.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.common.net.HttpHeaders;
import com.infosys.infytel.customer.service.CustomerAsyncDemoService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@RibbonClient(name="custribbon")
public class CustomerController {
	
	@Autowired
	private CustomerAsyncDemoService service;
	
	@Autowired
    private RestTemplate restTemplate;

    @Autowired
    @LoadBalanced
    private RestTemplate loadBalanced;
    
    @Autowired
    //DiscoveryClient client;  //for eureka without without loadbalancing //check customer controller
    RestTemplate template;  //for eureka without without loadbalancing
    
	
	@RequestMapping("/customer")
	public String getCustomer() {
		return "customer";
	}
	
	@GetMapping("/customerFriends")
	public List<Long> getCustomerFriends(){
		
		//normal loadbalancing
		List<Long> friends = loadBalanced.getForObject("http://custribbon/friendNumbers", List.class);
		
		
		//with eurekaserver load balancing
		List<Long> friends1=template.getForObject("http://FRIENDFAMILYMS"+"/friendNumbers", List.class);
		return friends;
	}
	
	@HystrixCommand(fallbackMethod="getmyCustomerFriendsFallback")
	@GetMapping("/myCustomerFriends/{num}")
	public List<Integer> getmyCustomerFriends(@PathVariable Integer num){
		System.out.println("== In customer friends ===");
		//with eurekaserver load balancing
		List<Integer> friends=new RestTemplate().getForObject("http://localhost:8301"+"/friendNumbers/"+num, List.class);
		return friends;
	}
	
	public List<Integer> getmyCustomerFriendsFallback(Integer num){
		System.out.println("=== In fall back ====");
		return new ArrayList<Integer>();
	}
	
	
	@RequestMapping("/myCustSync")
	public String myCustSync() {
		
		System.out.println("==start in myCustSync==");
		service.getFriends();
		service.getPlans();
		System.out.println("==end in myCustSync==");
		return "success";
	}
	
	@RequestMapping("/myCustAsync")
	public String myCustAsync() throws InterruptedException, ExecutionException {
		
		System.out.println("==start in myCustAsync==");
		Future<Integer> a = service.getFriendsforAsync();
		Future<Integer> b=service.getPlansforAsync();
		System.out.println(a.get());
		System.out.println(b.get());
		System.out.println("==end in myCustAsync==");
		return "success";
	}
	
	
	//the below request should come with zuul , then only headers will work
	//bcz headers information will come from zuul server
	@RequestMapping("/myCustOauth")
	public String myCustOauth(@RequestHeader HttpHeaders headers) throws InterruptedException, ExecutionException {
		
		//OAuth fails with direct resttemplate
		//List<Integer> list = new RestTemplate().getForObject("http://localhost:8400/plansSync", List.class);
		
		//send headers with rest exchange
		HttpEntity<String> entity = null;
		//HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<List> list = new RestTemplate().exchange("http://localhost:8400/plansSync",HttpMethod.GET, entity, List.class);
		
		return "success";
	}

}

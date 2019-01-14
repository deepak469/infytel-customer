package com.infosys.infytel.customer.service;

import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.infosys.infytel.customer.controller.CustFeign;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;

@Service
public class CustomerAsyncDemoService {
	
	@Autowired
	CustFeign feign;
	
	//@HystrixCommand
	public List<Integer> getFriends(){
		return new RestTemplate().getForObject("http://localhost:8301/friendsSync", List.class);
	}
	
	//@HystrixCommand
	public List<Integer> getPlans(){
		return new RestTemplate().getForObject("http://localhost:8400/plansSync", List.class);
	}
	
	//@HystrixCommand
		public Future<Integer> getFriendsforAsync(){
			
			return new AsyncResult<Integer>() {
				@Override
				public Integer invoke() {
					return new RestTemplate().getForObject("http://localhost:8301/friendsAsync", Integer.class);
				}
			};
			
		}
		
		//@HystrixCommand
		//the below method uses feign
		public Future<Integer> getPlansforAsync(){
			return new AsyncResult<Integer>() {
				@Override
				public Integer invoke() {
					//return new RestTemplate().getForObject("http://localhost:8400/plansAsync", Integer.class);
					return feign.getPlan();
				}
			};
		}
	
	

}

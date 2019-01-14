package com.infosys.infytel.customer.controller;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("infytelplanMS")
public interface CustFeign {
	
	@RequestMapping("/plansAsync")
	Integer getPlan();

}

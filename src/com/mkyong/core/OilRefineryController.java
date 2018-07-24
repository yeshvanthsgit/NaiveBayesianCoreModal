package com.mkyong.core;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mkyong.core.RefinaryLevelPredictor;

@RestController
@CrossOrigin("*")
public class OilRefineryController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/runModal")
    public RefineryPredictionStatus predict() {
    	RefineryPredictionStatus json = new RefineryPredictionStatus();
    	try {
			RefinaryLevelPredictor.predict();
			json.setContent("SUCCESS");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			json.setContent("FAILED");
		}
    	return json;
    }
}

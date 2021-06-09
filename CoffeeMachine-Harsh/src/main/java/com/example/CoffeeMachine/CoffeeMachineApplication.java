package com.example.CoffeeMachine;

import com.example.CoffeeMachine.Model.Beverage;
import com.example.CoffeeMachine.Model.Machine;
import com.example.CoffeeMachine.thread.CoffeeMachineThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class CoffeeMachineApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(CoffeeMachineApplication.class, args);
		Config config = (Config)  ctx.getBean("config");
		Machine machine = (Machine) ctx.getBean("machine");
		ReentrantLock reentrantLock = (ReentrantLock) ctx.getBean("reentrantLock");

		//Less outlets than beverages with not enough ingredients for each beverage
		System.out.println();
		System.out.println("************  Test Case 1 (when less outlets than beverages with not enough ingredients for each beverage) Started ************");

		ExecutorService executorService = Executors.newFixedThreadPool(machine.getMachineOutlets());
		for(Beverage beverage:machine.getBeverages()){
			CoffeeMachineThread coffeeMachineThread = new CoffeeMachineThread(reentrantLock,machine,beverage);
			executorService.execute(coffeeMachineThread);
		}
		executorService.shutdown();
		while(!executorService.isTerminated()){}

		//Less outlets than beverages with enough ingredients for each beverage
		System.out.println();
		System.out.println("************  Test Case 2 (when less outlets than beverages with enough ingredients for each beverage) Started ************");
		executorService = Executors.newFixedThreadPool(machine.getMachineOutlets());
		machine.refillAllIngredients( 1000);
		machine.refillIngredient("green_mixture",100);
		System.out.println();
		for(Beverage beverage:machine.getBeverages()){
			CoffeeMachineThread coffeeMachineThread = new CoffeeMachineThread(reentrantLock,machine,beverage);
			executorService.execute(coffeeMachineThread);
		}
		executorService.shutdown();
		while(!executorService.isTerminated()){}

		//Less outlets than beverages with enough ingredients for each beverage
		System.out.println();
		System.out.println("************  Test Case 3 (when beverage orders are provided as list) Started ************");
		executorService = Executors.newFixedThreadPool(machine.getMachineOutlets());
		machine.refillIngredient("green_mixture",100);
		machine.refillIngredient("hot_water",100);
		System.out.println();
		List<String> beveragesToPrepare = new ArrayList<>(Arrays.asList("hot_tea","hot_coffee","cold_coffee", "black_tea", "green_tea"));
		Map<String,Beverage> beverageMap = machine.prepareBeverageMap();
		for(int i=0;i<beveragesToPrepare.size();i++){
			if(!beverageMap.containsKey(beveragesToPrepare.get(i))) {
				System.out.println(beveragesToPrepare.get(i) + " cannot be prepared due to absence of required ingredients and recipe!!");
				System.out.println();
				continue;
			}
			CoffeeMachineThread coffeeMachineThread = new CoffeeMachineThread(reentrantLock,machine,beverageMap.get(beveragesToPrepare.get(i)));
			executorService.execute(coffeeMachineThread);
		}
		executorService.shutdown();
		while(!executorService.isTerminated()){}

	}

}

package com.example.CoffeeMachine;

import com.example.CoffeeMachine.Model.Beverage;
import com.example.CoffeeMachine.Model.Machine;
import com.example.CoffeeMachine.thread.CoffeeMachineThread;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;


@SpringBootTest
class CoffeeMachineApplicationTests {

	private ObjectMapper objectMapper = new ObjectMapper();
	private ReentrantLock reentrantLock = new ReentrantLock();
	private Machine machine = new Machine(objectMapper, reentrantLock);
	private Map<String, Beverage> beverageMap = prepareBeverageMap();

	//Coffee Machine having 3 Outlets and 4 beverages to prepare with insufficient ingredients
	@Test
	public void fourBeverage_InsufficientIngredients(){
		List<String> beveragesToPrepare = new ArrayList<>(Arrays.asList("hot_tea","hot_coffee","black_tea", "green_tea"));
		try {
			runTestForGivenBeverages(beveragesToPrepare);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Coffee Machine having 3 Outlets and 3 beverages to prepare with sufficient ingredients
	@Test
	public void threeBeverage_SufficientIngredients(){
		List<String> beveragesToPrepare = new ArrayList<>(Arrays.asList("hot_tea","hot_coffee","black_tea", "green_tea"));
		try {
			machine.refillAllIngredients( 1000);
			runTestForGivenBeverages(beveragesToPrepare);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Coffee Machine having 3 Outlets and 4 beverages to prepare with sufficient ingredients
	@Test
	public void fourBeverage_SufficientIngredients(){
		List<String> beveragesToPrepare = new ArrayList<>(Arrays.asList("hot_tea","hot_coffee","black_tea", "green_tea"));
		try {
			machine.refillAllIngredients( 1000);
			machine.refillIngredient("green_mixture",100);
			runTestForGivenBeverages(beveragesToPrepare);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String,Beverage> prepareBeverageMap(){
		List<Beverage> beverages = machine.getBeverages();
		Map<String,Beverage> tempBeverageMap = new HashMap<>();
		for(Beverage beverage:beverages){
			tempBeverageMap.put(beverage.getBeverageName(),beverage);
		}
		return tempBeverageMap;
	}

	private void runTestForGivenBeverages(List<String> beveragesToPrepare){

		//Making executor service equal to outlets, so n beverages can be processed in parallel
		ExecutorService executorService = Executors.newFixedThreadPool(machine.getMachineOutlets());

		for(int i=0;i<beveragesToPrepare.size();i++){
			if(!beverageMap.containsKey(beveragesToPrepare.get(i))) {
				System.out.println(beveragesToPrepare.get(i) + " cannot be prepared due to absence of required ingredients and recipe!!");
				continue;
			}
			CoffeeMachineThread machineThread = new CoffeeMachineThread(reentrantLock,machine,beverageMap.get(beveragesToPrepare.get(i)));
			executorService.execute(machineThread);
		}

		executorService.shutdown();
		while(!executorService.isTerminated()){}
	}

}

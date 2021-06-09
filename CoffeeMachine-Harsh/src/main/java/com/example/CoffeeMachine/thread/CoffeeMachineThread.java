package com.example.CoffeeMachine.thread;

import com.example.CoffeeMachine.Model.Beverage;
import com.example.CoffeeMachine.Model.Machine;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class CoffeeMachineThread implements Runnable{

    ReentrantLock reentrantLock;
    Machine machine;
    Beverage beverage;

    @Autowired
    public CoffeeMachineThread(ReentrantLock reentrantLock, Machine machine, Beverage beverage){
        this.reentrantLock = reentrantLock;
        this.machine = machine;
        this.beverage = beverage;
    }

    //given a beverage this function will print whether the beverage can be made or not. each running thread will access critical section without interference from other threads.
    @Override
    public void run() {
        Map<String,Integer> availableIngredients = machine.getStockIngredients();
        Map<String,Integer> beverageRecipe = beverage.getRecipe();
        Random random = new Random();
        while(true) {
            if(reentrantLock.tryLock()){
                try {
                    for(Map.Entry<String,Integer> recipeIngredient : beverageRecipe.entrySet()){
                        String name = recipeIngredient.getKey();
                        Integer quantity = recipeIngredient.getValue();
                        if(!availableIngredients.containsKey(name)) {
                            System.out.println(beverage.getBeverageName() + " cannot be prepared because "+name+ " is not available");
                            machine.indicatorForAbsence(name);
                            System.out.println();
                            return;
                        } else if(availableIngredients.get(name)<quantity){
                            System.out.println(beverage.getBeverageName() + " cannot be prepared because "+name+ " is not sufficient");
                            machine.indicatorForLowQuantity(name);
                            System.out.println();
                            return;
                        }
                    }
                    for(Map.Entry<String,Integer> recipeIngredient : beverageRecipe.entrySet()){
                        String name = recipeIngredient.getKey();
                        Integer quantity = recipeIngredient.getValue();
                        availableIngredients.put(name,availableIngredients.get(name)-quantity);
                    }
                    System.out.println(beverage.getBeverageName() + " is prepared and is ready to be served!");
                    System.out.println();
                    Thread.sleep(random.nextInt(1000));
                    break;
                } catch (InterruptedException ex) {
                    System.out.println("Coffee machine outlet got interrupted");
                } finally {
                    reentrantLock.unlock();
                }
            }
        }
    }
}

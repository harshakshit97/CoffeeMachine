package com.example.CoffeeMachine.Model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Component
public class Machine {


    private ObjectMapper objectMapper;

    private ReentrantLock reentrantLock;

    private Integer machineOutlets;
    private Map<String,Integer> stockIngredients;
    private List<Beverage> beverages;

    @Autowired
    public Machine(ObjectMapper objectMapper, ReentrantLock reentrantLock){
        this.objectMapper = objectMapper;
        this.reentrantLock = reentrantLock;
        try {
           JsonNode machineNode = this.objectMapper.readTree(new File("./src/main/resources/ingredients.json"));
           buildMachine(machineNode);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //This will build the coffee machine with ingredients and recipe provided in json
    public void buildMachine(JsonNode machineNode){
        this.machineOutlets=machineNode.get("machine").get("outlets").get("count_n").asInt();
        this.stockIngredients = new HashMap<>();
        HashMap<String, Integer> jsonIngredientsMap = objectMapper.convertValue(machineNode.get("machine").get("total_items_quantity"), new TypeReference<HashMap<String, Integer>>() {});
        for(Map.Entry<String, Integer> jsonIngredientsMapEntry : jsonIngredientsMap.entrySet()){
            stockIngredients.put(jsonIngredientsMapEntry.getKey(), jsonIngredientsMapEntry.getValue());
        }
        this.beverages = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = machineNode.get("machine").get("beverages").fields();
        while(iterator.hasNext()){
            Map.Entry<String,JsonNode> entry = iterator.next();
            Beverage beverage = new Beverage();
            beverage.setBeverageName(entry.getKey());
            beverage.setRecipe(objectMapper.convertValue(entry.getValue(), new TypeReference<HashMap<String, Integer>>() {}));
            this.beverages.add(beverage);
        }
    }

    //This will signal that ingredient is running low
    public void indicatorForLowQuantity(String ingredientName){
        System.out.println(ingredientName + " is running low! Please refill it.");
    }

    //This will signal that ingredient is absent
    public void indicatorForAbsence(String ingredientName){
        System.out.println(ingredientName + " is not available! Please refill it.");
    }

    //Method to refill a particular ingredient
    public void refillIngredient(String ingredientName, int quantity){
        while(true) {
            if (reentrantLock.tryLock()) {
                try {
                    int availableQuantity = stockIngredients.getOrDefault(ingredientName, 0);
                    stockIngredients.put(ingredientName, availableQuantity + quantity);
                    System.out.println(ingredientName + " refilled.");
                    return;
                } finally {
                    reentrantLock.unlock();
                }
            }
        }
    }

    //Method to refill all ingredients by adding specified quantity
    public void refillAllIngredients(int quantity){
        while(true) {
            if (reentrantLock.tryLock()) {
                try {
                    for(String ingredient : stockIngredients.keySet()){
                        int availableQuantity = stockIngredients.get(ingredient);
                        stockIngredients.put(ingredient, availableQuantity + quantity);
                    }
                    System.out.println("All ingredients are refilled.");
                    return;
                } finally {
                    reentrantLock.unlock();
                }
            }
        }
    }

    //This will return a map of beverage name and beverage recipe present in the machine
    public Map<String,Beverage> prepareBeverageMap(){
        List<Beverage> beverages = getBeverages();
        Map<String,Beverage> tempBeverageMap = new HashMap<>();
        for(Beverage beverage:beverages){
            tempBeverageMap.put(beverage.getBeverageName(),beverage);
        }
        return tempBeverageMap;
    }

}

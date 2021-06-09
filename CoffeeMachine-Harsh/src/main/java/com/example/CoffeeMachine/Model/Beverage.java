package com.example.CoffeeMachine.Model;

import lombok.Data;
import lombok.Getter;

import java.util.HashMap;

@Data
public class Beverage {
    //name of Beverage will be used as Unique Identifier
    private String beverageName;

    //Map to maintain Recipe of beverage
    private HashMap<String, Integer> recipe;
}

package com.udacity.mybakingapp.persistence;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.mybakingapp.model.Ingredient;
import com.udacity.mybakingapp.model.Step;

import java.lang.reflect.Type;
import java.util.List;

/**
 * © 2015 .  This code is distributed pursuant to your  Mobile Application Developer License
 * Agreement and may be used solely in accordance with the terms and conditions set forth therein.
 *  provides this software on as "as is", "where is" basis, with all faults known and unknown.
 *  makes no warranty, express, statutory or implied, and explicitly disclaims the * *
 * warranties or merchantability, fitness for a particular purpose, any warranty of non-infringement
 * of any third party’s intellectual property rights, any warranty that the licensed * works will
 * meet the requirements of licensee or any other user, any warrantee that the software will be
 * error-free or will operate without interruption, and any warranty that the software will
 * interoperate with any licensee or third party hardware, software or systems.  undertakes
 * no obligation whatsoever to support or maintain all or any part of this software.
 * The software is not fault tolerant and is not designed, intended or authorized for use in any
 * medical, lifesaving or life sustaining systems, or any other application in which the failure
 * of the licensed work could create a situation where personal injury or death may occur.
 * <p>
 * All other rights are reserved.
 **/
public class DataConverter {

    @TypeConverter
    public String fromIngredientsList(List<Ingredient> ingredients) {
        if (ingredients == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Ingredient>>() {}.getType();
        return gson.toJson(ingredients, type);
    }

    @TypeConverter
    public List<Ingredient> toIngredientsList(String ingredients) {
        if (ingredients == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Ingredient>>() {}.getType();
        return gson.fromJson(ingredients, type);
    }

    @TypeConverter
    public String fromStepsList(List<Step> steps) {
        if (steps == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Step>>() {}.getType();
        return gson.toJson(steps, type);
    }

    @TypeConverter
    public List<Step> toStepsLangList(String steps) {
        if (steps == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Step>>() {}.getType();
        return gson.fromJson(steps, type);
    }
}
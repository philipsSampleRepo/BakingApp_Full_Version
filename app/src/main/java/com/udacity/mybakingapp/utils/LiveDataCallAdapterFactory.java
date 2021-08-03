package com.udacity.mybakingapp.utils;

/*
  © 2015 .  This code is distributed pursuant to your  Mobile Application Developer License
  Agreement and may be used solely in accordance with the terms and conditions set forth therein.
   provides this software on as "as is", "where is" basis, with all faults known and unknown.
   makes no warranty, express, statutory or implied, and explicitly disclaims the * *
  warranties or merchantability, fitness for a particular purpose, any warranty of non-infringement
  of any third party’s intellectual property rights, any warranty that the licensed * works will
  meet the requirements of licensee or any other user, any warrantee that the software will be
  error-free or will operate without interruption, and any warranty that the software will
  interoperate with any licensee or third party hardware, software or systems.  undertakes
  no obligation whatsoever to support or maintain all or any part of this software.
  The software is not fault tolerant and is not designed, intended or authorized for use in any
  medical, lifesaving or life sustaining systems, or any other application in which the failure
  of the licensed work could create a situation where personal injury or death may occur.
  <p>
  All other rights are reserved.
 */
import androidx.lifecycle.LiveData;

import com.udacity.mybakingapp.network.responses.ApiResponse;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public class LiveDataCallAdapterFactory extends CallAdapter.Factory {


    /**
     * This method performs a number of checks and then returns the Response type for the Retrofit requests.
     * (@bodyType is the ResponseType. It can be RecipeResponse or RecipeSearchResponse)
     *
     * CHECK #1) returnType returns LiveData
     * CHECK #2) Type LiveData<T> is of ApiResponse.class
     * CHECK #3) Make sure ApiResponse is parameterized. AKA: ApiResponse<T> exists.
     *
     *
     */
    @Override
    public CallAdapter<?, ?> get(@NotNull Type returnType, @NotNull Annotation[] annotations, @NotNull Retrofit retrofit) {

        // Check #1
        // Make sure the CallAdapter is returning a type of LiveData
        if(CallAdapter.Factory.getRawType(returnType) != LiveData.class){
            return null;
        }

        // Check #2
        // Type that LiveData is wrapping
        Type observableType = CallAdapter.Factory.getParameterUpperBound(0, (ParameterizedType) returnType);

        // Check if it's of Type ApiResponse
        Type rawObservableType = CallAdapter.Factory.getRawType(observableType);
        if(rawObservableType != ApiResponse.class){
            throw new IllegalArgumentException("Type must be a defined resource");
        }

        // Check #3
        // Check if ApiResponse is parameterized. AKA: Does ApiResponse<T> exists? (must wrap around T)
        // FYI: T is either RecipeResponse or T will be a RecipeSearchResponse
        if(!(observableType instanceof ParameterizedType)){
            throw new IllegalArgumentException("resource must be parameterized");
        }

        Type bodyType = CallAdapter.Factory.getParameterUpperBound(0, (ParameterizedType) observableType);
        return new LiveDataCallAdapter<Type>(bodyType);
    }
}








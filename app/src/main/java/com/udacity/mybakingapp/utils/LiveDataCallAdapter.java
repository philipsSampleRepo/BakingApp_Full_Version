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

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveDataCallAdapter<R> implements CallAdapter<R, LiveData<ApiResponse<R>>> {

    private final Type responseType;

    public LiveDataCallAdapter(Type responseType) {
        this.responseType = responseType;
    }

    @NotNull
    @Override
    public Type responseType() {
        return responseType;
    }

    @NotNull
    @Override
    public LiveData<ApiResponse<R>> adapt(@NotNull final Call<R> call) {
        return new LiveData<ApiResponse<R>>(){
            @Override
            protected void onActive() {
                super.onActive();
                final ApiResponse apiResponse = new ApiResponse();
                if(!call.isExecuted()){
                    call.enqueue(new Callback<R>() {
                        @Override
                        public void onResponse(@NotNull Call<R> call, @NotNull Response<R> response) {
                            postValue(apiResponse.create(response));
                        }

                        @Override
                        public void onFailure(@NotNull Call<R> call, @NotNull Throwable t) {
                            postValue(apiResponse.create(t));
                        }
                    });
                }

            }
        };
    }

}

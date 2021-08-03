package com.udacity.mybakingapp.network.responses;

import android.util.Log;

import com.udacity.mybakingapp.model.ResponseModel;

import java.io.IOException;

import retrofit2.Response;

/**
 * © 2015 .  This code is distributed pursuant to your  Mobile Application Developer License
 * Agreement and may be used solely in accordance with the terms and conditions set forth therein.
 * provides this software on as "as is", "where is" basis, with all faults known and unknown.
 * makes no warranty, express, statutory or implied, and explicitly disclaims the * *
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
public class ApiResponse<T> {

    private static final String TAG = "ApiResponse";

    public ApiResponse<T> create(Throwable error) {
        if (error != null) {
            Log.e(TAG, error.getMessage());
        }
        return new ApiErrorResponse<>(error == null || error.getMessage().equals("")
                ? "Unknown error\nCheck network connection" : error.getMessage());
    }

    public ApiResponse<T> create(Response<T> response) {
        String errorMsg = "Error Occurred Please Try Again...";
        if (response == null || response.body() == null) {
            return new ApiErrorResponse<>(errorMsg);
        }

        if (response.isSuccessful()) {
            T body = response.body();

            if (body instanceof ResponseModel) {
                if (!CheckRecipeValidity.isValidResponse((ResponseModel) body)) {
                     errorMsg = "Api key is invalid or expired.";
                    return new ApiErrorResponse<>(errorMsg);
                }
            }

            if (body == null || response.code() == 204) { // 204 is empty response
                return new ApiEmptyResponse<>();
            } else {
                return new ApiSuccessResponse<>(body);
            }
        } else {
             if(response.errorBody() == null){
                 return new ApiErrorResponse<>(errorMsg);
             }
            try {
                errorMsg = response.errorBody().string();
            } catch (IOException e) {
                e.printStackTrace();
                errorMsg = response.message();
            }
            return new ApiErrorResponse<>(errorMsg);
        }
    }

    /**
     * Generic success response from api
     *
     * @param <T>
     */
    public static class ApiSuccessResponse<T> extends ApiResponse<T> {

        private final T body;

        ApiSuccessResponse(T body) {
            this.body = body;
        }

        public T getBody() {
            return body;
        }

    }

    /**
     * Generic Error response from API
     *
     * @param <T>
     */
    public static class ApiErrorResponse<T> extends ApiResponse<T> {

        private final String errorMessage;

        ApiErrorResponse(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

    }


    /**
     * separate class for HTTP 204 responses so that we can make ApiSuccessResponse's body non-null.
     */
    public static class ApiEmptyResponse<T> extends ApiResponse<T> {
    }

}



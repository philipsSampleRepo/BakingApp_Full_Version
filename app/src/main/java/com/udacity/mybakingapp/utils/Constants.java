package com.udacity.mybakingapp.utils;

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
public final class Constants {
    public static final String BASE_URL =
            "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/";

    public static final int CONNECTION_TIMEOUT = 10; // 10 seconds
    public static final int READ_TIMEOUT = 2; // 2 seconds
    public static final int WRITE_TIMEOUT = 2; // 2 seconds

    public static final String IMAGE_1 =
            "https://res.cloudinary.com/dk4ocuiwa/image/upload/v1575163942/RecipesApi/" +
                    "chocolatepieb23d.jpg";
    public static final String IMAGE_2 =
            "https://res.cloudinary.com/dk4ocuiwa/image/upload/v1575163942/RecipesApi/" +
                    "7810497752_23b973b33305e3.jpg";
    public static final String IMAGE_3 =
            "https://res.cloudinary.com/dk4ocuiwa/image/upload/v1575163942/RecipesApi/" +
                    "148569476.jpg";
    public static final String IMAGE_4 = "" +
            "https://res.cloudinary.com/dk4ocuiwa/image/upload/v1575163942/RecipesApi/" +
            "smorescheesecakeaf8f.jpg";


    public static final String LAST_CHOSEN_RECIPE_ID = "lastChosenRecipeId";
    public static final String LAST_CHOSEN_RECIPE_NAME = "lastChosenRecipeName";
    public static final String WIDGET_PREFERENCES = "widgetPreferences";
}

package com.udacity.mybankingapp;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.udacity.mybakingapp.ui.activities.RecipeListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

/**
 * © 2015.  This code is distributed pursuant to your  Mobile Application Developer License
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
@RunWith(AndroidJUnit4.class)
public class IdlingResourceRecipeListTest {

    private IdlingResource recipeIdlingResource;
    private String matchingLbl = "Brownies";

    @Before
    public void registerIdlingResource() {
        ActivityScenario activityScenario = ActivityScenario.launch(RecipeListActivity.class);
        activityScenario.onActivity(new ActivityScenario.ActivityAction<RecipeListActivity>() {
            @Override
            public void perform(RecipeListActivity activity) {
                recipeIdlingResource = activity.getIdlingResource();
                IdlingRegistry.getInstance().register(recipeIdlingResource);
            }
        });
    }

    @Test
    public void idlingResourceTest() {

        onView(withId(R.id.recipe_list))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(matchingLbl)),
                        click()));
        onView(withId(R.id.recipe_title)).check(matches(withText(matchingLbl)));
    }

    @After
    public void unregisterIdlingResource() {
        if (recipeIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(recipeIdlingResource);
        }
    }
}

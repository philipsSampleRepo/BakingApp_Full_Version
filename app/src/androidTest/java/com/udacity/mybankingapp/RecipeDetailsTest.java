package com.udacity.mybankingapp;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.udacity.mybakingapp.ui.activities.DetailsActivity;
import com.udacity.mybakingapp.ui.activities.RecipeListActivity;
import com.udacity.mybakingapp.utils.Constants;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasKey;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

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
@RunWith(AndroidJUnit4.class)
public class RecipeDetailsTest {

    private IdlingResource recipeIdlingResource;
    private final String MATCHINGLBL = "Brownies";
    private final String MATCHINGLCHEESECAKE = "Cheesecake";
    private final String INGREDIENTSTITLE = "Ingredients";
    private final String DESCRIPTION = "Recipe Introduction";
    private ActivityScenario activityScenario;

    @Before
    public void registerIdlingResource() {

        activityScenario = ActivityScenario.launch(RecipeListActivity.class);
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
                        hasDescendant(withText(MATCHINGLBL)),
                        click()));
        onView(withId(R.id.recipe_title)).check(matches(withText(MATCHINGLBL)));
        onView(withId(R.id.ingredients_title)).check(matches(withText(INGREDIENTSTITLE)));
        onView(withId(R.id.txt_short_description_val)).check(matches(withText(DESCRIPTION)));
        pressBack();

        onView(withId(R.id.recipe_list))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(MATCHINGLCHEESECAKE)),
                        click()));

        onView(withId(R.id.recipe_title)).check(matches(withText(MATCHINGLCHEESECAKE)));
        onView(withId(R.id.ingredients_title)).check(matches(withText(INGREDIENTSTITLE)));
    }

    @After
    public void unregisterIdlingResource() {
        if (recipeIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(recipeIdlingResource);
        }
    }
}

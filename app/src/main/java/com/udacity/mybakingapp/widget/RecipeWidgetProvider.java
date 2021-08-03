package com.udacity.mybakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.core.app.TaskStackBuilder;

import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybakingapp.ui.activities.DetailsActivity;
import com.udacity.mybakingapp.ui.activities.RecipeListActivity;
import com.udacity.mybakingapp.utils.Constants;
import com.udacity.mybankingapp.R;

import static com.udacity.mybakingapp.ui.activities.RecipeListActivity.RECIPE;

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
public class RecipeWidgetProvider extends AppWidgetProvider {

    public static boolean isTabletView = false;
    public static RecipeModel recipeModel;

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_widget_layout);

        if (recipeModel != null) {
            views.setTextViewText(R.id.appwidget_recipe_name, recipeModel.getName());
        }

        Intent intent = new Intent(context, RecipeWidgetRemoteViewsService.class);
        views.setRemoteAdapter(R.id.appwidget_ingredient_list, intent);
        views.setEmptyView(R.id.appwidget_ingredient_list, R.id.empty_tv_widget);

        if (recipeModel != null) {
            if (!isTabletView) {
                launchDetailsActivity(context, views);
            } else {
                launchRecipeListActivity(context, views);
            }
        } else {
            launchRecipeListActivity(context, views);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.appwidget_ingredient_list);
    }

    private static void launchDetailsActivity(Context context, RemoteViews views) {
        Intent detailsIntent = new Intent(context, DetailsActivity.class);
        detailsIntent.putExtra(RECIPE, recipeModel);

        PendingIntent appPendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(detailsIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_root, appPendingIntent);
    }

    private static void launchRecipeListActivity(Context context, RemoteViews views) {
        Intent mainIntent = new Intent(context, RecipeListActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0,
                mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_root, appPendingIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void triggerUpdate(Context context, RecipeModel model) {
        recipeModel = model;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, RecipeWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.appwidget_ingredient_list);
        for (int appWidgetId : appWidgetIds) {
            RecipeWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(Constants.WIDGET_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.LAST_CHOSEN_RECIPE_ID);
        editor.remove(Constants.LAST_CHOSEN_RECIPE_NAME);
        editor.apply();
    }
}

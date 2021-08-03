package com.udacity.mybakingapp.widget;

import android.content.Context;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.mybakingapp.model.Ingredient;
import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybakingapp.ui.frgments.RecipeListFragment;
import com.udacity.mybankingapp.R;

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
    public class RecipeRemoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = RecipeRemoteWidgetFactory.class.getSimpleName();
    private final Context mContext;
    private RecipeModel list;

    public RecipeRemoteWidgetFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        list = RecipeListFragment.getInstance().getWidgetModel();
        Log.d(TAG, "onDataSetChanged: ");
        if (list != null && list.getIngredients() != null && list.getIngredients().size() != 0) {
            Ingredient ingredient = list.getIngredients().get(list.getIngredients().size() - 1);
            Log.d(TAG, "RecipeRemoteWidgetFactory: Ingredient " + "Name: "+list.getName()
                    + " " + ingredient.getIngredient());
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.getIngredients().size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                list == null || list.getIngredients().get(position) == null) {
            return null;
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.recipe_widget_list_item);
        Ingredient currentIngredient = list.getIngredients().get(position);
        //  Ingredient currentIngredient = mIngredientList.get(position);

        views.setTextViewText(R.id.widget_item_quantity, String.valueOf(currentIngredient.getQuantity()));
        views.setTextViewText(R.id.widget_item_measure, currentIngredient.getMeasure());
        views.setTextViewText(R.id.widget_item_ingredient, currentIngredient.getIngredient());
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

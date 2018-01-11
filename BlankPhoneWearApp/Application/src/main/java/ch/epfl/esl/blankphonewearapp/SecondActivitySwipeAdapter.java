package ch.epfl.esl.blankphonewearapp;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Claudio on 10.01.2018.
 */

public class SecondActivitySwipeAdapter extends SmartFragmentStatePagerAdapter {

    private static int NUM_ITEMS = 2;

    public SecondActivitySwipeAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new FirstFragmentSwipe();
            case 1:
                return new SecondFragmentSwipe();
            default:
                return null;
        }
    }



}


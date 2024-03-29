package smartlife.monitorwearables.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import smartlife.monitorwearables.fragments.miband.TabFragment1;
import smartlife.monitorwearables.fragments.miband.TabFragment2;
import smartlife.monitorwearables.fragments.miband.TabFragment3;

public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {
    int numberOfTabs;
    List<Fragment> registeredFragments;

    public DemoCollectionPagerAdapter(FragmentManager fm, int numTabs, List<Fragment> fragments) {
        super(fm);
        numberOfTabs = numTabs;
        registeredFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                if(registeredFragments.size() > 0 && registeredFragments.get(0) != null)
                    return registeredFragments.get(0);
                else
                    return TabFragment1.newInstance();
            case 1:
                if(registeredFragments.size() > 0 && registeredFragments.get(1) != null)
                    return registeredFragments.get(1);
                else
                    return TabFragment2.newInstance();
            case 2:
                if(registeredFragments.size() > 0 && registeredFragments.get(2) != null)
                    return registeredFragments.get(2);
                else
                    return TabFragment3.newInstance();
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "TAB " + (position + 1);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

}
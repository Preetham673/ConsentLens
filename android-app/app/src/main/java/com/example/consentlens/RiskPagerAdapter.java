package com.example.consentlens;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RiskPagerAdapter extends FragmentStateAdapter {

    public RiskPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {

            case 0:
                return new OverviewFragment();

            case 1:
                return new DataFragment();

            case 2:
                return new AIFragment();

            case 3:
                return new LegalFragment();

            case 4:
                return new AdviceFragment();

            default:
                return new OverviewFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
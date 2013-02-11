package com.kedzie.vbox.app;

import android.content.res.Configuration;
import android.os.Bundle;


public class FragmentActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE 
                && Utils.getScreenSize(getResources().getConfiguration())>=Configuration.SCREENLAYOUT_SIZE_LARGE)
            finish();
        
        FragmentElement element = (FragmentElement)getIntent().getParcelableExtra(FragmentElement.BUNDLE);
        
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(element.name);
        
        if(savedInstanceState==null) {
        	Utils.addFragment(this, getSupportFragmentManager(), android.R.id.content, element);
        }
    }
}

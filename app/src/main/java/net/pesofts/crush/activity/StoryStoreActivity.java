package net.pesofts.crush.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import net.pesofts.crush.R;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.fragment.StoryDetailFragment;
import net.pesofts.crush.fragment.StoryStoreFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by erkas on 2017. 5. 8..
 */

public class StoryStoreActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_story_store);

        String userId = getIntent().getStringExtra("userId");
        String gender = getIntent().getStringExtra("gender");

        getSupportFragmentManager().beginTransaction()
                .add(R.id.id_fragment_container, StoryStoreFragment.newInstance(userId, gender), "")
//                .add(R.id.id_fragment_container, StoryDetailFragment.newInstance(userId, null), StoryDetailFragment.TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE));
        super.onBackPressed();
    }
}

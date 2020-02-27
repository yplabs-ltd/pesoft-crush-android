package net.pesofts.crush.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import net.pesofts.crush.R;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.fragment.StoryDetailFragment;
import net.pesofts.crush.model.StoryData;

import de.greenrobot.event.EventBus;

/**
 * Created by erkas on 2017. 5. 11..
 */

public class StoryDetailActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_story_store);

        String userId = getIntent().getStringExtra("userId");
        String gender = getIntent().getStringExtra("gender");
        StoryData storyData = getIntent().getParcelableExtra("storyData");

        getSupportFragmentManager().beginTransaction()
                .add(R.id.id_fragment_container, StoryDetailFragment.newInstance(userId, gender, storyData), StoryDetailFragment.TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE));
        super.onBackPressed();
    }
}

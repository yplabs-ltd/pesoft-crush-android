package net.pesofts.crush.fragment;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.pesofts.crush.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProgressDialogFragment extends BaseDialogFragment {

    @Bind(R.id.progress_image)
    ImageView progressImage;

    public static ProgressDialogFragment newInstance() {
        Bundle args = new Bundle();

        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_fragment_progress, container, false);
        ButterKnife.bind(this, contentView);

        ((Animatable) progressImage.getDrawable()).start();

        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}

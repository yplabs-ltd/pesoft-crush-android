package net.pesofts.crush.adapter;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.fragment.ProfileCodeDialogFragment;
import net.pesofts.crush.model.UserInfoCode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileCodeAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<UserInfoCode> codeList;
    private String selectedValue;
    private ProfileCodeDialogFragment.SelectCodeListener selectCodeListener;
    private DialogFragment dialog;

    public class ProfileCodeHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.code_layout)
        RelativeLayout codeLayout;
        @Bind(R.id.title_text)
        TextView titleText;

        public ProfileCodeHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public ProfileCodeAdapter(Context context, DialogFragment dialog, List<UserInfoCode> codeList, String selectedValue, ProfileCodeDialogFragment.SelectCodeListener selectCodeListener) {
        this.context = context;
        this.codeList = codeList;
        this.selectedValue = selectedValue;
        this.selectCodeListener = selectCodeListener;
        this.dialog = dialog;
    }

    @Override
    public ProfileCodeHolder onCreateViewHolder(ViewGroup parent,
                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_code, parent, false);
        ProfileCodeHolder vh = new ProfileCodeHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final UserInfoCode userInfoCode = codeList.get(position);
        ProfileCodeHolder profileViewHolder = (ProfileCodeHolder) holder;

        if (codeList.size() <= 6) {
            ViewGroup.LayoutParams layoutParams = profileViewHolder.codeLayout.getLayoutParams();
            layoutParams.height = context.getResources().getDimensionPixelSize(R.dimen.profile_code_dialog_height_small);
        } else {
            ViewGroup.LayoutParams layoutParams = profileViewHolder.codeLayout.getLayoutParams();
            layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        }

        profileViewHolder.titleText.setText(userInfoCode.getValue());
        if (userInfoCode.getValue().equals(selectedValue)) {
            profileViewHolder.titleText.setTextColor(ContextCompat.getColor(context, R.color.main_red_color));
        } else {
            profileViewHolder.titleText.setTextColor(ContextCompat.getColor(context, R.color.dimmed_text_color));
        }
        profileViewHolder.codeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectCodeListener != null) {
                    selectCodeListener.onSelectCode(userInfoCode);
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return codeList.size();
    }

}

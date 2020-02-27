package net.pesofts.crush.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.model.UserInfoCode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MultiProfileCodeAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<UserInfoCode> codeList;
    private List<UserInfoCode> selectedCodeList;

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

    public List<UserInfoCode> getSelectedCodeList(){
        return selectedCodeList;
    }

    public MultiProfileCodeAdapter(Context context, List<UserInfoCode> codeList, List<UserInfoCode> selectedCodeList) {
        this.context = context;
        this.codeList = codeList;

        if (selectedCodeList != null) {
            this.selectedCodeList = selectedCodeList;
        } else {
            this.selectedCodeList = new ArrayList<>();
        }
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
        final ProfileCodeHolder profileViewHolder = (ProfileCodeHolder) holder;

        if (codeList.size() <= 6) {
            ViewGroup.LayoutParams layoutParams = profileViewHolder.codeLayout.getLayoutParams();
            layoutParams.height = context.getResources().getDimensionPixelSize(R.dimen.profile_code_dialog_height_small);
        } else {
            ViewGroup.LayoutParams layoutParams = profileViewHolder.codeLayout.getLayoutParams();
            layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        }

        profileViewHolder.titleText.setText(userInfoCode.getValue());

        if (isSelectedValue(userInfoCode)) {
            profileViewHolder.titleText.setTextColor(ContextCompat.getColor(context, R.color.main_red_color));
        } else {
            profileViewHolder.titleText.setTextColor(ContextCompat.getColor(context, R.color.dimmed_text_color));
        }
        profileViewHolder.codeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSelectedValue(userInfoCode)) {
                    removeSelectedCode(userInfoCode);
                    profileViewHolder.titleText.setTextColor(ContextCompat.getColor(context, R.color.dimmed_text_color));
                } else {
                    selectedCodeList.add(userInfoCode);
                    profileViewHolder.titleText.setTextColor(ContextCompat.getColor(context, R.color.main_red_color));
                }

            }
        });

    }

    private boolean isSelectedValue(UserInfoCode userInfoCode) {
        for (UserInfoCode selectedCode : selectedCodeList) {
            if (selectedCode.getValue().equals(userInfoCode.getValue())) {
                return true;
            }
        }

        return false;
    }

    private void removeSelectedCode(UserInfoCode userInfoCode) {
        for (UserInfoCode selectedCode : selectedCodeList) {
            if (selectedCode.getValue().equals(userInfoCode.getValue())) {
                selectedCodeList.remove(selectedCode);
                return;
            }
        }
    }

    @Override
    public int getItemCount() {
        return codeList.size();
    }

}

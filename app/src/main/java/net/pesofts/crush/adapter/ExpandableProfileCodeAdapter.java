package net.pesofts.crush.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import net.pesofts.crush.R;
import net.pesofts.crush.fragment.ProfileCodeDialogFragment;
import net.pesofts.crush.model.UserInfoCode;
import net.pesofts.crush.model.UserInfoCodeGroup;

import java.util.List;

public class ExpandableProfileCodeAdapter extends ExpandableRecyclerAdapter<ExpandableProfileCodeAdapter.GroupTitleHolder, ExpandableProfileCodeAdapter.UserInfoCodeViewHolder> {

    private Context context;
    private LayoutInflater mInflator;
    private String selectedValue;
    private ProfileCodeDialogFragment.SelectCodeListener selectCodeListener;
    private ProfileCodeDialogFragment.OpenSubCodeListener openSubCodeListener;
    private DialogFragment dialog;

    public ExpandableProfileCodeAdapter(Context context, DialogFragment dialog, @NonNull List<? extends ParentListItem> parentItemList, String selectedValue, ProfileCodeDialogFragment.SelectCodeListener selectCodeListener, ProfileCodeDialogFragment.OpenSubCodeListener openSubCodeListener) {
        super(parentItemList);

        mInflator = LayoutInflater.from(context);
        this.context = context;
        this.selectedValue = selectedValue;
        this.selectCodeListener = selectCodeListener;
        this.openSubCodeListener = openSubCodeListener;
        this.dialog = dialog;
    }

    @Override
    public ExpandableProfileCodeAdapter.GroupTitleHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View groupTitleView = mInflator.inflate(R.layout.item_profile_code, parentViewGroup, false);
        return new ExpandableProfileCodeAdapter.GroupTitleHolder(groupTitleView);
    }

    @Override
    public ExpandableProfileCodeAdapter.UserInfoCodeViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View userInfoCodeView = mInflator.inflate(R.layout.item_sub_profile_code, childViewGroup, false);
        return new ExpandableProfileCodeAdapter.UserInfoCodeViewHolder(userInfoCodeView);
    }

    @Override
    public void onBindParentViewHolder(ExpandableProfileCodeAdapter.GroupTitleHolder groupTitleHolder, int position, ParentListItem parentListItem) {
        UserInfoCodeGroup userInfoCodeGroup = (UserInfoCodeGroup) parentListItem;
        groupTitleHolder.bind(userInfoCodeGroup);
    }

    @Override
    public void onBindChildViewHolder(ExpandableProfileCodeAdapter.UserInfoCodeViewHolder userInfoCodeViewHolder, int position, Object childListItem) {
        UserInfoCode userInfoCode = (UserInfoCode) childListItem;
        userInfoCodeViewHolder.bind(userInfoCode);
    }

    public class GroupTitleHolder extends ParentViewHolder {

        private TextView titleText;

        public GroupTitleHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
        }

        public void bind(UserInfoCodeGroup userInfoCodeGroup) {
            titleText.setText(userInfoCodeGroup.getTitle());
            if (userInfoCodeGroup.getTitle().equals(selectedValue)) {
                titleText.setTextColor(ContextCompat.getColor(context, R.color.main_red_color));
            } else {
                titleText.setTextColor(ContextCompat.getColor(context, R.color.dimmed_text_color));
            }
        }

        @Override
        public void setExpanded(boolean expanded) {
            super.setExpanded(expanded);

            if (expanded) {
                openSubCodeListener.onOpenSubCode(getAdapterPosition());
            }
        }
    }

    public class UserInfoCodeViewHolder extends ChildViewHolder {

        private View codeLayout;
        private TextView titleText;

        public UserInfoCodeViewHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
            codeLayout = itemView.findViewById(R.id.code_layout);
        }

        public void bind(final UserInfoCode userInfoCode) {
            titleText.setText(userInfoCode.getValue());
            codeLayout.setOnClickListener(new View.OnClickListener() {
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
    }
}


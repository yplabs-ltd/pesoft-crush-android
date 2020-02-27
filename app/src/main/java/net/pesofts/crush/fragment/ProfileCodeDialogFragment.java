package net.pesofts.crush.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.adapter.ExpandableProfileCodeAdapter;
import net.pesofts.crush.adapter.ProfileCodeAdapter;
import net.pesofts.crush.model.UserInfoCode;
import net.pesofts.crush.model.UserInfoCodeGroup;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileCodeDialogFragment extends BaseDialogFragment {

    @Bind(R.id.select_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.title_text)
    TextView titleText;

    private List<UserInfoCode> codeList;
    private List<UserInfoCodeGroup> codeGroup;
    private String selectedValue;
    private String title;
    private SelectCodeListener selectCodeListener;
    private OpenSubCodeListener openSubCodeListener;
    private Integer selectPosition;
    private LinearLayoutManager linearLayoutManager;

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSelectPosition(Integer selectPosition) {
        this.selectPosition = selectPosition;
    }

    public void setCodeGroup(List<UserInfoCodeGroup> codeGroup) {
        this.codeGroup = codeGroup;
    }

    public interface SelectCodeListener {
        void onSelectCode(UserInfoCode userInfoCode);
    }

    public void setSelectCodeListener(SelectCodeListener selectCodeListener) {
        this.selectCodeListener = selectCodeListener;
    }

    public interface OpenSubCodeListener {
        void onOpenSubCode(int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_profile, container, false);
        ButterKnife.bind(this, contentView);

        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (title != null) {
            titleText.setText(title);
        }

        if (codeList != null) {
            ProfileCodeAdapter adapter = new ProfileCodeAdapter(getActivity(), this, codeList, selectedValue, selectCodeListener);
            recyclerView.setAdapter(adapter);

            if (codeList.size() <= 6) {
                ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
                layoutParams.height = getResources().getDimensionPixelSize(R.dimen.profile_code_dialog_height_small) * codeList.size();
            }
        }

        if (codeGroup != null) {
            openSubCodeListener = new OpenSubCodeListener() {
                @Override
                public void onOpenSubCode(int position) {
                    if (position != 0) {

                        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                        int diff = position - firstPosition;
                        View firstChild = recyclerView.getChildAt(0);
                        if (firstChild != null) {
                            if (diff != 0) {
                                if (diff == 1) {
                                    recyclerView.smoothScrollBy(0, firstChild.getHeight());
                                } else {
                                    recyclerView.smoothScrollBy(0, firstChild.getHeight() + firstChild.getHeight() / 2);
                                }
                            }
                        }
                    }
                }
            };
            ExpandableProfileCodeAdapter expandableProfileCodeAdapter = new ExpandableProfileCodeAdapter(getActivity(), this, codeGroup, selectedValue, selectCodeListener, openSubCodeListener);
            recyclerView.setAdapter(expandableProfileCodeAdapter);
        }

        if (selectPosition != null) {
            linearLayoutManager.scrollToPosition(selectPosition);
        }

    }

    public void setCodeList(List<UserInfoCode> codeList) {
        this.codeList = codeList;
    }
}

package net.pesofts.crush.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.adapter.MultiProfileCodeAdapter;
import net.pesofts.crush.model.UserInfoCode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MultiProfileCodeDialogFragment extends BaseDialogFragment {

    @Bind(R.id.select_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.title_text)
    TextView titleText;

    private List<UserInfoCode> codeList;
    private List<UserInfoCode> selectedCodeList;
    private String title;
    private SelectCodeListener selectCodeListener;
    private LinearLayoutManager linearLayoutManager;
    private MultiProfileCodeAdapter adapter;

    public void setSelectedCodeList(List<UserInfoCode> selectedCodeList) {
        this.selectedCodeList = selectedCodeList;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public interface SelectCodeListener {
        void onSelectCode(List<UserInfoCode> userInfoCodeList);
    }

    public void setSelectCodeListener(SelectCodeListener selectCodeListener) {
        this.selectCodeListener = selectCodeListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_multi_profile, container, false);
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
            adapter = new MultiProfileCodeAdapter(getActivity(), codeList, selectedCodeList);
            recyclerView.setAdapter(adapter);
        }

    }

    public void setCodeList(List<UserInfoCode> codeList) {
        this.codeList = codeList;
    }

    @OnClick(R.id.confirm_button)
    void clickConfirm(View view) {
        if (selectCodeListener != null && adapter != null) {
            selectCodeListener.onSelectCode(adapter.getSelectedCodeList());
        }

        dismiss();
    }

    @OnClick(R.id.cancel_button)
    void clickCancel(View view) {
        dismiss();
    }
}

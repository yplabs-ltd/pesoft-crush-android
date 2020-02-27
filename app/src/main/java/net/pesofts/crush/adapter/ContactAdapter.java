package net.pesofts.crush.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.Util.SnackbarUtil;
import net.pesofts.crush.model.Contact;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContactAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<Contact> contactList;
    private List<Contact> checkContactList = new ArrayList<>();

    private OnContactClickListener onContactClickListener;

    public interface OnContactClickListener {
        void onContactClick(List<Contact> checkContactList);
    }

    public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
        this.onContactClickListener = onContactClickListener;
    }

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.contact_layout)
        View contactLayout;
        @Bind(R.id.title_text)
        TextView titleText;
        @Bind(R.id.check_button)
        ImageButton checkButton;

        public ContactHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent,
                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        ContactHolder vh = new ContactHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Contact contact = contactList.get(position);
        final ContactHolder contactHolder = (ContactHolder) holder;

        contactHolder.titleText.setText(contact.getName());
        if (checkContactList.indexOf(contact) < 0) {
            contactHolder.checkButton.setImageResource(R.drawable.freeshop_btn_check_off);
        } else {
            contactHolder.checkButton.setImageResource(R.drawable.freeshop_btn_check_on);
        }
        contactHolder.contactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkContactList.indexOf(contact) < 0) {
                    if (checkContactList.size() >= 10) {
                        SnackbarUtil.getSnackbar(contactHolder.contactLayout, context.getString(R.string.limit_number_alert)).show();
                        return;
                    }
                    checkContactList.add(contact);
                    contactHolder.checkButton.setImageResource(R.drawable.freeshop_btn_check_on);
                } else {
                    checkContactList.remove(contact);
                    contactHolder.checkButton.setImageResource(R.drawable.freeshop_btn_check_off);
                }

                onContactClickListener.onContactClick(checkContactList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

}

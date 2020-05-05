package com.example.assignment03;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/* Dialog that appears when an invalid address has been entered */
public class InvalidAddressDialog extends DialogFragment {

    private TextView actionOK; //textview for closing the dialog

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInsatnceState) {
        View view = inflater.inflate(R.layout.invalid_address_dialog, container, false);
        //setting view background to transparent to use rounded window
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionOK =view.findViewById(R.id.dialog_action_ok);
        actionOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(getArguments() != null )
            this.getArguments().clear();

    }
}

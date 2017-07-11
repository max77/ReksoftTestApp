package com.example.mkomarovskiy.reksofttestapp.ui.map;

import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mkomarovskiy.reksofttestapp.R;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 10/07/2017.
 */

public class MyPlaceSelectionFragment extends SupportPlaceAutocompleteFragment {

    private TextView mAddressLine;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(layoutInflater, viewGroup, bundle);

        view.setBackgroundColor(getResources().getColor(R.color.colorAddressInputBackground));

        EditText searchInput = view.findViewById(R.id.place_autocomplete_search_input);
        ViewGroup.LayoutParams params = searchInput.getLayoutParams();
        ViewGroup parent = ((ViewGroup) searchInput.getParent());
        searchInput.setVisibility(View.GONE);

        mAddressLine = new AppCompatTextView(getContext());
        mAddressLine.setGravity(Gravity.CENTER_VERTICAL);
        mAddressLine.setHint(searchInput.getHint());
        mAddressLine.setTextColor(getResources().getColor(R.color.colorText));
        mAddressLine.setHintTextColor(getResources().getColor(R.color.colorHint));

        int minSize = getResources().getDimensionPixelSize(R.dimen.textsize_address_min);
        int maxSize = getResources().getDimensionPixelSize(R.dimen.textsize_address_max);
        int step = getResources().getDimensionPixelSize(R.dimen.textsize_address_step);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mAddressLine, minSize, maxSize, step, TypedValue.COMPLEX_UNIT_PX);
        mAddressLine.setOnClickListener(view1 -> searchInput.callOnClick());

        parent.addView(mAddressLine, parent.indexOfChild(searchInput), params);
        return view;
    }

    @Override
    public void setText(CharSequence charSequence) {
        super.setText(charSequence);
        mAddressLine.setText(charSequence);
    }
}

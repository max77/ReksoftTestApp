package com.example.mkomarovskiy.reksofttestapp.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mkomarovskiy.reksofttestapp.R;
import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;

import java.util.List;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 11/07/2017.
 */

class AddressListAdapter extends RecyclerView.Adapter<AddressListAdapter.ViewHolder> {

    private List<ILocationInfo> mLocations;
    private long mSelectedId = -1;
    private Listener mListener;
    private boolean areItemsSelectable;
    private long mSelectedItemId;

    public AddressListAdapter(Listener listener, boolean areItemsSelectable) {
        mListener = listener;
        this.areItemsSelectable = areItemsSelectable;
        setHasStableIds(true);
    }

    public void setData(List<ILocationInfo> locations) {
        mLocations = locations;
        notifyDataSetChanged();
    }

    public void setSelectedItemId(long id) {
        int oldPos = findItemPositionById(mSelectedId);

        mSelectedId = id;
        int newPos = findItemPositionById(mSelectedId);

        if (oldPos != -1)
            notifyItemChanged(oldPos);

        if (newPos != -1)
            notifyItemChanged(newPos);
    }

    @Override
    public long getItemId(int position) {
        return mLocations.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_address, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ILocationInfo item = mLocations.get(position);

        holder.mAddress.setText(item.getAddress());

        if (areItemsSelectable)
            highlightItem(holder.itemView, item.getId() == mSelectedId);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null)
                mListener.onItemSelected(item);
        });
    }

    private void highlightItem(View itemView, boolean hilite) {
        itemView.setBackgroundColor(itemView.getResources()
                .getColor(hilite ? R.color.colorSelectedAddressBG : R.color.colorNormalAddressBG));
    }

    @Override
    public int getItemCount() {
        return mLocations != null ? mLocations.size() : 0;
    }

    public int findItemPositionById(long id) {
        for (int i = 0; i < mLocations.size(); i++)
            if (id == mLocations.get(i).getId())
                return i;

        return -1;
    }

    public void addItem(ILocationInfo location) {
        mLocations.add(location);
        notifyItemChanged(mLocations.size() - 1);
    }

    public long getSelectedItemId() {
        return mSelectedItemId;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            mAddress = itemView.findViewById(R.id.address);
        }
    }

    public interface Listener {
        void onItemSelected(ILocationInfo item);
    }
}

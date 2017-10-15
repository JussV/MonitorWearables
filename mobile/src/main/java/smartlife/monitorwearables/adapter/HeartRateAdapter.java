package smartlife.monitorwearables.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import smartlife.monitorwearables.R;
import smartlife.monitorwearables.entities.HeartRate;

/**
 * Created by Joana on 9/25/2017.
 */

public class HeartRateAdapter extends RecyclerView.Adapter<HeartRateAdapter.ViewHolder> {

    private List<HeartRate> mDataset;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView hrTextView;
        public TextView dateTextView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            hrTextView = (TextView) itemView.findViewById(R.id.tv_heart_rate);
            dateTextView = (TextView) itemView.findViewById(R.id.tv_date);
        }
    }

    public HeartRateAdapter(List<HeartRate> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HeartRateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // create a new view
        View  v = inflater.inflate(R.layout.item_heart_rate, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.hrTextView.setText(String.valueOf(mDataset.get(position).getValue()));
        holder.dateTextView.setText(String.valueOf(mDataset.get(position).getCreatedAt()));

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}

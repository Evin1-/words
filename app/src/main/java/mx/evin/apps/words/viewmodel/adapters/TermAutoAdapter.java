package mx.evin.apps.words.viewmodel.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mx.evin.apps.words.R;

/**
 * Created by evin on 12/19/15.
 */
public class TermAutoAdapter extends RecyclerView.Adapter<TermAutoAdapter.ViewHolder> implements Filterable{

    private static List<String> mOriginalTerms;
    private static List<String> mFilteredTerms;
    private static final String TAG_ = "TermAutoAdapterTAG_";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTerm;

        public ViewHolder(final View itemView) {
            super(itemView);

            txtTerm = (TextView) itemView.findViewById(R.id.txtTerm);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    EditText editText = (EditText) v.getRootView().findViewById(R.id.editInputTerm);
                    if (editText != null){
                        Log.d(TAG_, "Clicked: " + v);
                    }
                }
            });
        }
    }

    public TermAutoAdapter(List<String> terms) {
        mOriginalTerms = terms;
        mFilteredTerms = terms;
    }

    @Override
    public TermAutoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View termView = inflater.inflate(R.layout.recycler_autocomplete_term, parent, false);

        return new ViewHolder(termView);
    }

    @Override
    public void onBindViewHolder(TermAutoAdapter.ViewHolder viewHolder, int position) {
        String term = mFilteredTerms.get(position);

        TextView textRelated = viewHolder.txtTerm;
        textRelated.setText(term);
    }

    @Override
    public int getItemCount() {
        return mFilteredTerms.size();
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<String> results = new ArrayList<>();

                for (String s : mOriginalTerms){
                    if (s.toLowerCase().contains(constraint)){
                        results.add(s);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = results;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredTerms = (List<String>) results.values;
                notifyDataSetChanged();
            }
        };

        return filter;
    }
}
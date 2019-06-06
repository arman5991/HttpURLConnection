package json.com.json;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private List<Android> modelList;

    RecyclerViewAdapter(List<Android> modelList) {
        this.modelList = modelList;
    }

    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_row_item, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.version.setText(modelList.get(position).getVer());
        holder.name.setText(modelList.get(position).getName());
        holder.api.setText(modelList.get(position).getApi());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView version;
        private TextView name;
        private TextView api;

        private MyViewHolder(View view) {
            super(view);

            version = view.findViewById(R.id.tv_version);
            name = view.findViewById(R.id.tv_name);
            api = view.findViewById(R.id.tv_api);
        }
    }
}
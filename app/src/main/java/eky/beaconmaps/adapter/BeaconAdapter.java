package eky.beaconmaps.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import eky.beaconmaps.R;

/**
 * Created by Enes Kamil YILMAZ on 28.03.2019.
 */

public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.ViewHolder> {

    private List<Beacon> beaconList;
    DecimalFormat numberFormat = new DecimalFormat("#.00");

    public BeaconAdapter(List<Beacon> beaconList) {
        this.beaconList = beaconList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Beacon beacon = beaconList.get(position);

        holder.tvUuid.setText("Uuid : " + beacon.getId1());
        holder.tvMajor.setText("Major : " + beacon.getId2());
        holder.tvMinor.setText("Minor : " + beacon.getId3());
        holder.tvDistance.setText(Double.toString(beacon.getDistance()).substring(0,5));
        holder.tvRssi.setText("Rssi : " + beacon.getRssi());
        holder.tvTx.setText("Rx : " + beacon.getTxPower());

    }

    @Override
    public int getItemCount() {
        return beaconList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUuid;
        TextView tvMajor;
        TextView tvMinor;
        TextView tvDistance;
        TextView tvRssi;
        TextView tvTx;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(false);

            tvUuid = itemView.findViewById(R.id.proximity_uuid);
            tvMajor = itemView.findViewById(R.id.major);
            tvMinor = itemView.findViewById(R.id.minor);
            tvDistance = itemView.findViewById(R.id.distance);
            tvRssi = itemView.findViewById(R.id.rssi);
            tvTx = itemView.findViewById(R.id.tx);
        }
    }

}

package com.lastshot.vrumvrumdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RVCarAdapter extends RecyclerView.Adapter<RVCarAdapter.MyViewHolder> {
    List<Car> carList;
    Context context;
    DatabaseHelper databaseHelper;

    public RVCarAdapter(List<Car> carList, Context context) {
        this.carList = carList;
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_line_car, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.car = carList.get(position);
        holder.carName.setText(carList.get(position).getName());
        holder.carCurrentFuelLevel.setText(String.valueOf(carList.get(position).getCurrentFuelLevel()));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchActivity.class);
                intent.putExtra("id", carList.get(position).getCarId());
                //Toast.makeText(context,  String.valueOf(carList.get(position).getCarId()), Toast.LENGTH_SHORT).show();
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView carName;
        EditText carCurrentFuelLevel;
        ConstraintLayout parentLayout;
        Car car;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            carName = itemView.findViewById(R.id.car_item);
            carCurrentFuelLevel = itemView.findViewById(R.id.fuel_level);
            parentLayout = itemView.findViewById(R.id.one_line_car);
            carCurrentFuelLevel.setEnabled(false);

            itemView.findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseHelper.deleteOne(car);
                    itemView.findViewById(R.id.one_line_car).setVisibility(View.INVISIBLE);
                }
            });

            itemView.findViewById(R.id.update_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    carCurrentFuelLevel.setEnabled(true);
                    itemView.findViewById(R.id.save_update_btn).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.delete_btn).setVisibility(View.VISIBLE);
                }
            });

            itemView.findViewById(R.id.save_update_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseHelper.updateOne(car, String.valueOf(carCurrentFuelLevel.getText()));
                    carCurrentFuelLevel.setEnabled(false);
                    itemView.findViewById(R.id.save_update_btn).setVisibility(View.INVISIBLE);
                    itemView.findViewById(R.id.delete_btn).setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}

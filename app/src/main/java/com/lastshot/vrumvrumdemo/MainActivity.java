package com.lastshot.vrumvrumdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button AddCarBtn;
    private Button deleteBtn;
    private Button updateBtn;
    private RecyclerView AllCars;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AddCarBtn = (Button) findViewById(R.id.add_car_btn);
        AddCarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddCarActivity.class);
                startActivity(i);
            }
        });

        // Show all cars in RecycleView
        DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
        List<Car> allCars = databaseHelper.getAllCars();
        mRecyclerView = findViewById(R.id.car_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RVCarAdapter(allCars, this);
        mRecyclerView.setAdapter(mAdapter);
    }
}
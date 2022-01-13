package com.lastshot.vrumvrumdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddCarActivity extends AppCompatActivity {

    private Button SaveBtn;
    private EditText NewCarName;
    private EditText NewCarFuel;
    private EditText NewCarMileage;
    private EditText NewCarFuelLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        NewCarName = findViewById(R.id.new_car_name);
        NewCarFuel = findViewById(R.id.new_car_fuel);
        NewCarMileage = findViewById(R.id.new_car_mileage);
        NewCarFuelLevel = findViewById(R.id.new_car_fuel_level);

        SaveBtn = (Button) findViewById(R.id.save_btn);
        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Car newCar;
                try {
                    newCar = new Car(NewCarName.getText().toString(), NewCarFuel.getText().toString(), Double.parseDouble(NewCarMileage.getText().toString()), Double.parseDouble(NewCarFuelLevel.getText().toString()));
                } catch (Exception e) {
                    newCar = new Car();
                    Toast.makeText(AddCarActivity.this, "errorrrr", Toast.LENGTH_SHORT).show();
                }

                DatabaseHelper databaseHelper = new DatabaseHelper(AddCarActivity.this);
                boolean success = databaseHelper.addOne(newCar);
                //Toast.makeText(AddCarActivity.this, "Success=" + success, Toast.LENGTH_SHORT).show();

                Intent i = new Intent(AddCarActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}
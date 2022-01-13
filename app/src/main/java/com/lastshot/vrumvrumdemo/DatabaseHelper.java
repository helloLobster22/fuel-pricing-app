package com.lastshot.vrumvrumdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String CAR_TABLE = "CAR_TABLE";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_FUEL = "FUEL";
    public static final String COLUMN_FUEL_LEVEL = "CURRENTFUELLEVEL";
    public static final String COLUMN_MILEAGE = "MILEAGE";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "fuelapp.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + CAR_TABLE + " (CARID INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_FUEL + " TEXT, " + COLUMN_FUEL_LEVEL + " DOUBLE, " + COLUMN_MILEAGE + " DOUBLE)";
        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOne(Car car) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, car.getName());
        cv.put(COLUMN_FUEL, car.getFuelType());
        cv.put(COLUMN_FUEL_LEVEL, car.getCurrentFuelLevel());
        cv.put(COLUMN_MILEAGE, car.getMileage());

        long insert = db.insert(CAR_TABLE, null, cv);

        db.close();

        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public List<Car> getAllCars() {
        List<Car> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + CAR_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                int carId = cursor.getInt(0);
                String carName = cursor.getString(1);
                Double carMilage = cursor.getDouble(4);
                Double carCurrentFuel = cursor.getDouble(3);

                Car car = new Car(carId, carName, carMilage, carCurrentFuel);
                returnList.add(car);
            } while (cursor.moveToNext());
        } else {

        }

        cursor.close();
        db.close();

        return returnList;
    }

    public Car getOne(int id) {
        Car returnCar = null;

        String queryString = "SELECT * FROM " + CAR_TABLE + " WHERE CARID = " + id;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.moveToFirst()) {
            returnCar = new Car(cursor.getInt(0), cursor.getString(1), cursor.getDouble(4), cursor.getDouble(3));
        } else {

        }

        cursor.close();
        db.close();

        return returnCar;
    }

    public boolean deleteOne(Car car) {
        //Toast.makeText(context, car.getName(), Toast.LENGTH_SHORT).show();
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + CAR_TABLE + " WHERE CARID = " + car.getCarId();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    public void updateOne(Car car, String newFuelLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_FUEL_LEVEL, Double.parseDouble(newFuelLevel));

        db.update(CAR_TABLE, cv, "CARID=?", new String[]{String.valueOf(car.getCarId())});
        System.out.println(car.getCurrentFuelLevel());

        db.close();
    }
}

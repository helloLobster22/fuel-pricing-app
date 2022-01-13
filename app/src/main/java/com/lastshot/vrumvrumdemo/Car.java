package com.lastshot.vrumvrumdemo;

public class Car {
    private int carId;
    private String name;
    private String fuelType;
    private double currentFuelLevel;
    private double mileage;
    private double tankCapacity; //TODO

    public Car(String name, String fuelType, double mileage, double fuelLevel) {
        this.name = name;
        this.fuelType = fuelType;
        this.currentFuelLevel = fuelLevel;
        this.mileage = mileage;
    }

    public Car(int id, String name, double mileage, double fuelLevel) {
        this.carId = id;
        this.name = name;
        this.fuelType = "";
        this.currentFuelLevel = fuelLevel;
        this.mileage = mileage;
    }

    public Car() {
        this.name = "";
        this.fuelType = "";
        this.currentFuelLevel = 0.0;
        this.mileage = 0.0;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public double getCurrentFuelLevel() {
        return currentFuelLevel;
    }

    public void setCurrentFuelLevel(double currentFuelLevel) {
        this.currentFuelLevel = currentFuelLevel;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public double getTankCapacity() {
        return tankCapacity;
    }

    public void setTankCapacity(double tankCapacity) {
        this.tankCapacity = tankCapacity;
    }

    @Override
    public String toString() {
        return this.carId + ": " + this.name;
    }
}

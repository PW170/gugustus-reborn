package com.gugustus.settings;

public class NumberSetting extends Setting {

    private double value;
    private double minimum;
    private double maximum;
    private double increment;

    public NumberSetting(String name, double defaultValue, double minimum, double maximum, double increment) {
        super(name);
        this.value = defaultValue;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = Math.max(minimum, Math.min(maximum, value));
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public double getIncrement() {
        return increment;
    }

    public int getValueInt() {
        return (int) value;
    }
}

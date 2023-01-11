package org.sudo.tools.utils;

import lombok.Setter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TaskProgress {
    private final PropertyChangeSupport propertyChangeSupport;
    private List<PropertyChangeListener> listeners;
    @Setter
    private String taskName;
    @Setter
    private int totalSteps;
    private int currentStep;
    private double percentage;
    private String error;
    private String progress;

    public TaskProgress() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.listeners = new ArrayList<>();

        this.taskName = "";
        this.totalSteps = 0;
        this.currentStep = 0;
        this.percentage = 0;
        this.error = "";
        this.progress = "";
    }

    public TaskProgress(String taskName, int totalSteps) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.listeners = new ArrayList<>();

        this.taskName = taskName;
        this.totalSteps = totalSteps;
        this.currentStep = 0;
        this.error = "";
        this.progress = "";
    }

    public void addProgressUpdateListener(PropertyChangeListener listener) {
        this.listeners.add(listener);
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removeAllProgressUpdateListeners() {
        for (var listener : this.listeners) {
            this.propertyChangeSupport.removePropertyChangeListener(listener);
        }
        this.listeners = new ArrayList<>();
    }

    private void updateTaskProgress(int currentStep, String error) {
        String nextProgress;

        double nextPercentage = new BigDecimal(currentStep * 100.0 / this.totalSteps).setScale(2, RoundingMode.HALF_UP).doubleValue();

        if (nextPercentage == this.percentage) {
            return;
        }

        this.percentage = nextPercentage;

        if (error.isBlank()) {
            nextProgress = String.format(
                    "Task %s => progress: %5.02f percent",
                    this.taskName, this.percentage
            );
        } else {
            nextProgress = String.format(
                    "Task %s => progress: %5.02f percent, error: %s",
                    this.taskName, this.percentage, error
            );
        }

        this.propertyChangeSupport
                .firePropertyChange("progress", this.progress, nextProgress);
        this.progress = nextProgress;
    }

    public void setCurrentStep(int currentStep) {
        this.updateTaskProgress(currentStep, this.error);
        this.currentStep = currentStep;
    }

    public void setError(String error) {
        this.updateTaskProgress(this.currentStep, error);
        this.error = error;
    }
}

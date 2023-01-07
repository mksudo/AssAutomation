package org.sudo.tools.utils;

import lombok.Setter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    private String error;
    private String progress;

    public TaskProgress() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.listeners = new ArrayList<>();

        this.taskName = "";
        this.totalSteps = 0;
        this.currentStep = 0;
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
        if (error.isBlank()) {
            nextProgress = String.format(
                    "Task %s => progress: %d of %d",
                    this.taskName, currentStep, this.totalSteps
            );
        } else {
            nextProgress = String.format(
                    "Task %s => progress: %d/%d, error: %s",
                    this.taskName, currentStep, this.totalSteps, error
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

package com.techbeloved.alc40task;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.Objects;

public final class Task implements Parcelable {
    /*
     id: string
     title: string
     detail: string
     created: date
     updated: date
     completed:  boolean
     */

    private String id;

    private String title;

    private String detail;

    private Date updated;

    private boolean completed;

    public Task() {

    }

    public Task(String id, String title, String detail, Date updated, boolean completed) {
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.updated = updated;
        this.completed = completed;
    }

    protected Task(Parcel in) {
        id = in.readString();
        title = in.readString();
        detail = in.readString();
        completed = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(detail);
        dest.writeByte((byte) (completed ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public Date getUpdated() {
        return updated;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return "Task{" + "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", updated=" + updated +
                ", completed=" + completed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return completed == task.completed &&
                Objects.equals(id, task.id) &&
                Objects.equals(title, task.title) &&
                Objects.equals(detail, task.detail) &&
                Objects.equals(updated, task.updated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, detail, updated, completed);
    }
}

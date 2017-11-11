package smartlife.monitorwearables.entities;

import java.util.Date;


public class HeartRate {

    private int id;
    private int value;
    private Date createdAt;

    public HeartRate(){}

    public HeartRate(int value, Date createedAtDate){
        this.value = value;
        this.createdAt = createedAtDate;
    }

    public HeartRate(int id, int value, Date createedAtDate){
        this.id = id;
        this.value = value;
        this.createdAt = createedAtDate;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

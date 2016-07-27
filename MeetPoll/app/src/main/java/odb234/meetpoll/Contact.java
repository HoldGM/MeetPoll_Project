package odb234.meetpoll;

/**
 * Created by odb234 on 7/27/16.
 */
public class Contact{
    private String name;
    private String phone;
    private boolean safeState;

    public Contact(){}

    public Contact(String n, String p, boolean s){
        name = n;
        phone = p;
        safeState = s;
    }

    public String getName(){
        return name;
    }
    public String getPhone(){
        return phone;
    }
    public boolean getState(){
        return safeState;
    }
    public void setState(boolean b){
        this.safeState = b;
    }
    public void setName(String n){ this.name = n; }
    public void setPhone(String p){ this.phone = p; }
    public String toString(){
        return name + ", " + phone;
    }
}
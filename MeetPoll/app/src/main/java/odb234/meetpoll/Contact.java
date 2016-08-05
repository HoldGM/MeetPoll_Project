package odb234.meetpoll;

/**
 * Created by odb234 on 7/27/16.
 */
public class Contact{
    private String name;
    private String phone;
    private boolean safeState;
    private boolean voted;
    private String invitePath;

    public Contact(){}

    public Contact(String n, String p, boolean s){
        name = n;
        phone = p;
        safeState = s;
        voted = false;
        invitePath = "";
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
    public boolean getVoted() { return voted; }
    public void setState(boolean b){
        this.safeState = b;
    }
    public String getInvitePath(){return  invitePath; }
    public void setName(String n){ this.name = n; }
    public void setPhone(String p){ this.phone = p; }
    public void setVoted(boolean v){ voted = v; }
    public void setInvitePath(String p){ invitePath  = p; }
    public String toString(){
        return name + ", " + phone;
    }
}
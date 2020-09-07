package ch.epfl.esl.blankphonewearapp;
import java.util.ArrayList;
/**
 * Created by fouco on 1/7/18.
 */

public class rackModel {

    private String headerTitle;
    private ArrayList<serverItem> ServersInRack;


    public rackModel() {

    }
    public rackModel(String headerTitle, ArrayList<serverItem> ServersInRack) {
        this.headerTitle = headerTitle;
        this.ServersInRack = ServersInRack;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public ArrayList<serverItem> getAllItemsInSection() {
        return ServersInRack;
    }

    public void setServers(ArrayList<serverItem> Servers) {
        this.ServersInRack = Servers;
    }

}

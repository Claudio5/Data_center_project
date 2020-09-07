package ch.epfl.esl.datacenter;

/**
 * Created by fouco on 1/10/18.
 */

public class ServerDataBase {

    private String id;
    private String series;

    public ServerDataBase(int rack, int server, String series){
        this.id= "Rack: "+rack+" Server: "+server;
        this.series=series;
    }

    public String getId(){
        return id;
    }

    public void setId(int rack, int server){
        this.id= "Rack: "+rack+" Server: "+server;
    }

    public void setSeries(String series){
        this.series=series;
    }

    public String getSeries(){
        return series;
    }
}

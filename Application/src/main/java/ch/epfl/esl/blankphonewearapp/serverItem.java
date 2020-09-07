package ch.epfl.esl.blankphonewearapp;

/**
 * Created by fouco on 1/7/18.
 */

public class serverItem {
    private CharSequence name;
    private boolean select;
    private int cpu;
    private String description;


    public serverItem() {
    }

    public serverItem(CharSequence name,int cpu, String description) {
        this.name = name;
        this.cpu = cpu;
        this.description = description;
    }

    public boolean getSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public CharSequence getName() {
        return  name;
    }

    public void setName(int num) {
        this.name = name;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int num) {
        this.cpu = num;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

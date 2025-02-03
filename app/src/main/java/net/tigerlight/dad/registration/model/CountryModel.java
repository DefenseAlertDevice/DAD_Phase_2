package net.tigerlight.dad.registration.model;

/**
 * Created by indianic on 28/03/17.
 */

public class CountryModel {

    int id;
    String c_c;
    String c_name;
    String c_e_no;


    //empty contructor
    public CountryModel() {

    }

    // constructor
    public CountryModel(int id, String c_c, String c_name, String c_e_no) {
        this.id = id;
        this.c_c = c_c;
        this.c_name = c_name;
        this.c_e_no = c_e_no;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getC_c() {
        return c_c;
    }

    public void setC_c(String c_c) {
        this.c_c = c_c;
    }

    public String getC_name() {
        return c_name;
    }

    public void setC_name(String c_name) {
        this.c_name = c_name;
    }

    public String getC_e_no() {
        return c_e_no;
    }

    public void setC_e_no(String c_e_no) {
        this.c_e_no = c_e_no;
    }
}

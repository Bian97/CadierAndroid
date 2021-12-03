package app.convencao.cadier.modelo;

import app.convencao.cadier.util.Enums.StatusEnum;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class User implements Serializable {
    private int physicalId;
    private String name;
    private String street;
    private String district;
    private String city;
    private String state;
    private String code;
    private String country;
    private String rg;
    private String cpf;
    private boolean gender;
    private String phone1;
    private String phone2;
    private String office;
    private Date birthDate;
    private Date joinDate;
    private String spouse;
    private String church;
    private String address;
    private String parentage;
    private String job;
    private String presidentName;
    private String email;
    private String presented;
    private Date updatedDate;
    //private String status;
    private StatusEnum status;
    private Date lastVisit;
    private String obs;
    private String photo;

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public int getPhysicalId() {
        return physicalId;
    }

    public void setPhysicalId(int physicalId) {
        this.physicalId = physicalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public String getSpouse() {
        return spouse;
    }

    public void setSpouse(String spouse) {
        this.spouse = spouse;
    }

    public String getChurch() {
        return church;
    }

    public void setChurch(String church) {
        this.church = church;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getParentage() {
        return parentage;
    }

    public void setParentage(String parentage) {
        this.parentage = parentage;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getPresidentName() {
        return presidentName;
    }

    public void setPresidentName(String presidentName) {
        this.presidentName = presidentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPresented() {
        return presented;
    }

    public void setPresented(String presented) {
        this.presented = presented;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    /*public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }*/

    public Date getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public User(int physicalId, String name, String phone1, String office, Date birthDate,
                String spouse, String parentage, String job, String presidentName,
                String email, String presented, String phone2, String photo) {

        this.physicalId = physicalId;
        this.name = name;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.office = office;
        this.birthDate = birthDate;
        this.spouse = spouse;
        this.parentage = parentage;
        this.job = job;
        this.presidentName = presidentName;
        this.email = email;
        this.presented = presented;
        this.photo = photo;
    }
    public void UserAddress(String street, String district, String city, String state, String code, String country){
        this.street = street;
        this.district = district;
        this.city = city;
        this.state = state;
        this.code = code;
        this.country = country;
    }

    public void UserInfos(String rg, String cpf){
        this.rg = rg;
        this.cpf = cpf;
    }

    public void UserSituations(Date joinDate, Date updatedDate, StatusEnum status, Date lastVisit, String obs){
        this.joinDate = joinDate;
        this.updatedDate = updatedDate;
        this.status = status;
        this.lastVisit = lastVisit;
        this.obs = obs;
    }

    public void UserChurch(String church, String address){
        this.church = church;
        this.address = address;
    }

    public User(){}
}

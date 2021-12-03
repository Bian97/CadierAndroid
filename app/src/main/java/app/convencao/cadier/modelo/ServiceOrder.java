package app.convencao.cadier.modelo;

import app.convencao.cadier.util.Enums.ServiceKindEnum;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by DrGreend on 31/03/2018.
 */

public class ServiceOrder implements Serializable {
    private int orderId, physicalId, attendantId;
    private String service, obs, whoTook;
    private float servicePrice, payedToday, previousCredit, remains, deposit;
    private ServiceKindEnum serviceKind;
    private Date monthly, orderDate, finishedDate, deliveryDate;

    public ServiceOrder(int orderId, int physicalId, int attendantId, String service, String obs, Date orderDate, Date finishedDate, Date deliveryDate, String whoTook, float servicePrice,
                        float payedToday, float previousCredit, float deposit, ServiceKindEnum serviceKind, Date monthly) {
        this.orderId = orderId;
        this.physicalId = physicalId;
        this.attendantId = attendantId;
        this.service = service;
        this.obs = obs;
        this.orderDate = orderDate;
        this.finishedDate = finishedDate;
        this.deliveryDate = deliveryDate;
        this.whoTook = whoTook;
        this.servicePrice = servicePrice;
        this.payedToday = payedToday;
        this.previousCredit = previousCredit;
        this.deposit = deposit;
        this.remains = servicePrice - (payedToday + previousCredit + deposit);
        this.serviceKind = serviceKind;
        this.monthly = monthly;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getPhysicalId() {
        return physicalId;
    }

    public void setPhysicalId(int physicalId) {
        this.physicalId = physicalId;
    }

    public int getAttendantId() {
        return attendantId;
    }

    public void setAttendantId(int attendantId) {
        this.attendantId = attendantId;
    }

    public ServiceKindEnum getServiceKind() {
        return serviceKind;
    }

    public void setServiceKind(ServiceKindEnum serviceKind) {
        this.serviceKind = serviceKind;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getWhoTook() {
        return whoTook;
    }

    public void setWhoTook(String whoTook) {
        this.whoTook = whoTook;
    }

    public float getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(float servicePrice) {
        this.servicePrice = servicePrice;
    }

    public float getPayedToday() {
        return payedToday;
    }

    public void setPayedToday(float payedToday) {
        this.payedToday = payedToday;
    }

    public float getPreviousCredit() {
        return previousCredit;
    }

    public void setPreviousCredit(float previousCredit) {
        this.previousCredit = previousCredit;
    }

    public float getRemains() {
        return remains;
    }

    public void setRemains(float remains) {
        this.remains = remains;
    }

    public float getDeposit() {
        return deposit;
    }

    public void setDeposit(float deposit) {
        this.deposit = deposit;
    }

    public Date getMonthly() {
        return monthly;
    }

    public void setMonthly(Date monthly) {
        this.monthly = monthly;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}

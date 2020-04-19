package com.example.cadier.modelo;

import java.io.Serializable;

/**
 * Created by DrGreend on 31/03/2018.
 */

public class OrdemServico implements Serializable {
    private int codOrd, fkRolFis, fkAtend;
    private String tipoServ, obs, dataSoli, dataFez, dataEnt, nomeLevou;
    private float valorServ, pgHj, creditoAnt, resta, deposito;

    public OrdemServico(int codOrd, int fkRolFis, int fkAtend, String tipoServ, String obs, String dataSoli, String dataFez, String dataEnt, String nomeLevou, float valorServ,
                        float pgHj, float creditoAnt, float resta, float deposito) {
        this.codOrd = codOrd;
        this.fkRolFis = fkRolFis;
        this.fkAtend = fkAtend;
        this.tipoServ = tipoServ;
        this.obs = obs;
        this.dataSoli = dataSoli;
        this.dataFez = dataFez;
        this.dataEnt = dataEnt;
        this.nomeLevou = nomeLevou;
        this.valorServ = valorServ;
        this.pgHj = pgHj;
        this.creditoAnt = creditoAnt;
        this.resta = resta;
        this.deposito = deposito;
    }

    public int getCodOrd() {
        return codOrd;
    }

    public void setCodOrd(int codOrd) {
        this.codOrd = codOrd;
    }

    public int getFkRolFis() {
        return fkRolFis;
    }

    public void setFkRolFis(int fkRolFis) {
        this.fkRolFis = fkRolFis;
    }

    public int getFkAtend() {
        return fkAtend;
    }

    public void setFkAtend(int fkAtend) {
        this.fkAtend = fkAtend;
    }

    public String getTipoServ() {
        return tipoServ;
    }

    public void setTipoServ(String tipoServ) {
        this.tipoServ = tipoServ;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public String getDataSoli() {
        return dataSoli;
    }

    public void setDataSoli(String dataSoli) {
        this.dataSoli = dataSoli;
    }

    public String getDataFez() {
        return dataFez;
    }

    public void setDataFez(String dataFez) {
        this.dataFez = dataFez;
    }

    public String getDataEnt() {
        return dataEnt;
    }

    public void setDataEnt(String dataEnt) {
        this.dataEnt = dataEnt;
    }

    public String getNomeLevou() {
        return nomeLevou;
    }

    public void setNomeLevou(String nomeLevou) {
        this.nomeLevou = nomeLevou;
    }

    public float getValorServ() {
        return valorServ;
    }

    public void setValorServ(float valorServ) {
        this.valorServ = valorServ;
    }

    public float getPgHj() {
        return pgHj;
    }

    public void setPgHj(float pgHj) {
        this.pgHj = pgHj;
    }

    public float getCreditoAnt() {
        return creditoAnt;
    }

    public void setCreditoAnt(float creditoAnt) {
        this.creditoAnt = creditoAnt;
    }

    public float getResta() {
        return resta;
    }

    public void setResta(float resta) {
        this.resta = resta;
    }

    public float getDeposito() {
        return deposito;
    }

    public void setDeposito(float deposito) {
        this.deposito = deposito;
    }
}

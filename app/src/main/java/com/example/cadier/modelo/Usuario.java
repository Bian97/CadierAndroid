package com.example.cadier.modelo;

import java.io.Serializable;

/**
 * Created by DrGreend on 07/03/2018.
 */

public class Usuario implements Serializable {
    private int rol;
    private String nome;
    private String rua;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String pais;
    private String rg;
    private String cpf;
    private boolean sexo;
    private String telefone;
    private String telOp;
    private String cargo;
    private String dataNasc;
    private String dataEntrou;
    private String conjuge;
    private String igreja;
    private String end;
    private String filiacao;
    private String titulo;
    private String profissao;
    private String nomePres;
    private String email;
    private String apres;
    private String dataAtuali;
    private String status;
    private String ultViz;
    private String obs;
    private String foto;

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
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

    public boolean isSexo() {
        return sexo;
    }

    public void setSexo(boolean sexo) {
        this.sexo = sexo;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getTelOp() {
        return telOp;
    }

    public void setTelOp(String telOp) {
        this.telOp = telOp;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(String dataNasc) {
        this.dataNasc = dataNasc;
    }

    public String getDataEntrou() {
        return dataEntrou;
    }

    public void setDataEntrou(String dataEntrou) {
        this.dataEntrou = dataEntrou;
    }

    public String getConjuge() {
        return conjuge;
    }

    public void setConjuge(String conjuge) {
        this.conjuge = conjuge;
    }

    public String getIgreja() {
        return igreja;
    }

    public void setIgreja(String igreja) {
        this.igreja = igreja;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getFiliacao() {
        return filiacao;
    }

    public void setFiliacao(String filiacao) {
        this.filiacao = filiacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public String getNomePres() {
        return nomePres;
    }

    public void setNomePres(String nomePres) {
        this.nomePres = nomePres;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApres() {
        return apres;
    }

    public void setApres(String apres) {
        this.apres = apres;
    }

    public String getDataAtuali() {
        return dataAtuali;
    }

    public void setDataAtuali(String dataAtuali) {
        this.dataAtuali = dataAtuali;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUltViz() {
        return ultViz;
    }

    public void setUltViz(String ultViz) {
        this.ultViz = ultViz;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Usuario(int rol, String nome, String rua, String bairro, String cidade, String estado, String cep, String pais,
                   String rg, String cpf, String telefone, String cargo, String dataNasc, String dataEntrou,
                   String conjuge, String igreja, String end, String filiacao, String titulo, String profissao, String nomePres,
                   String email, String apres, String dataAtuali, String status, String ultViz, String obs, String telOp, String foto) {

        this.rol = rol;
        this.nome = nome;
        this.rua = rua;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.pais = pais;
        this.rg = rg;
        this.cpf = cpf;
        this.telefone = telefone;
        this.telOp = telOp;
        this.cargo = cargo;
        this.dataNasc = dataNasc;
        this.dataEntrou = dataEntrou;
        this.conjuge = conjuge;
        this.igreja = igreja;
        this.end = end;
        this.filiacao = filiacao;
        this.titulo = titulo;
        this.profissao = profissao;
        this.nomePres = nomePres;
        this.email = email;
        this.apres = apres;
        this.dataAtuali = dataAtuali;
        this.status = status;
        this.ultViz = ultViz;
        this.obs = obs;
        this.foto = foto;
    }
    public Usuario(){}
}

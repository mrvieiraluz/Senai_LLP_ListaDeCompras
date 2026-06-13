package com.gestaocompras.model;

/**
 * Representa um item de uma lista de compras.
 */
public class Item {

    public enum Unidade {
        UN, CX, KG, LT;

        public static Unidade fromString(String valor) {
            if (valor == null) return null;
            try {
                return Unidade.valueOf(valor.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private String descricao;
    private Unidade unidade;
    private double quantidade;

    public Item(String descricao, Unidade unidade, double quantidade) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.quantidade = quantidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f %s)", descricao, quantidade, unidade);
    }
}

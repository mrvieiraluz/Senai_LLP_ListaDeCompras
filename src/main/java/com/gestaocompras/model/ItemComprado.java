package com.gestaocompras.model;

/**
 * Representa um item já comprado, com preço e quantidade efetiva.
 */
public class ItemComprado {

    private Item item;
    private double quantidadeComprada;
    private double precoUnitario;

    public ItemComprado(Item item, double quantidadeComprada, double precoUnitario) {
        this.item = item;
        this.quantidadeComprada = quantidadeComprada;
        this.precoUnitario = precoUnitario;
    }

    public Item getItem() {
        return item;
    }

    public double getQuantidadeComprada() {
        return quantidadeComprada;
    }

    public void setQuantidadeComprada(double quantidadeComprada) {
        this.quantidadeComprada = quantidadeComprada;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public double getTotalItem() {
        return quantidadeComprada * precoUnitario;
    }

    public boolean estaEmFalta() {
        return precoUnitario == 0.0;
    }
}

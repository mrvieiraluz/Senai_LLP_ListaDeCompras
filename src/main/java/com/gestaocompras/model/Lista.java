package com.gestaocompras.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma lista de compras com seus itens e registro da compra realizada.
 */
public class Lista {

    private String nome;
    private List<Item> itens;
    private List<ItemComprado> itensComprados;
    private boolean compraRealizada;

    public Lista(String nome) {
        this.nome = nome;
        this.itens = new ArrayList<>();
        this.itensComprados = new ArrayList<>();
        this.compraRealizada = false;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Item> getItens() {
        return itens;
    }

    public void adicionarItem(Item item) {
        this.itens.add(item);
    }

    public List<ItemComprado> getItensComprados() {
        return itensComprados;
    }

    public void setItensComprados(List<ItemComprado> itensComprados) {
        this.itensComprados = itensComprados;
        this.compraRealizada = !itensComprados.isEmpty();
    }

    public boolean isCompraRealizada() {
        return compraRealizada;
    }

    public void setCompraRealizada(boolean compraRealizada) {
        this.compraRealizada = compraRealizada;
    }

    public boolean temItens() {
        return !itens.isEmpty();
    }

    public double getTotalCompra() {
        return itensComprados.stream()
                .mapToDouble(ItemComprado::getTotalItem)
                .sum();
    }

    @Override
    public String toString() {
        return nome;
    }
}

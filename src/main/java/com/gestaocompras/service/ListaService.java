package com.gestaocompras.service;

import com.gestaocompras.model.Item;
import com.gestaocompras.model.ItemComprado;
import com.gestaocompras.model.Lista;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço com a lógica de negócio para gerenciamento de listas de compras.
 */
public class ListaService {

    private final ListaRepository repositorio;

    public ListaService() {
        this.repositorio = new ListaRepository();
    }

    public Lista criarLista(String nome) {
        if (repositorio.existeNome(nome)) {
            throw new IllegalArgumentException("Já existe uma lista com o nome: " + nome);
        }
        Lista lista = new Lista(nome);
        return lista;
    }

    public void adicionarItem(Lista lista, Item item) {
        lista.adicionarItem(item);
    }

    public void salvarLista(Lista lista) {
        repositorio.salvar(lista);
    }

    public List<Lista> listarTodas() {
        return repositorio.findAll();
    }

    public List<Lista> listarComItens() {
        List<Lista> todas = repositorio.findAll();
        List<Lista> comItens = new ArrayList<>();
        for (Lista l : todas) {
            if (l.temItens()) {
                comItens.add(l);
            }
        }
        return comItens;
    }

    public List<Lista> listarComCompraRealizada() {
        List<Lista> todas = repositorio.findAll();
        List<Lista> comCompra = new ArrayList<>();
        for (Lista l : todas) {
            if (l.isCompraRealizada()) {
                comCompra.add(l);
            }
        }
        return comCompra;
    }

    public void registrarCompra(Lista lista, List<ItemComprado> itensComprados) {
        lista.setItensComprados(itensComprados);
        lista.setCompraRealizada(true);
        repositorio.salvar(lista);
    }

    public Lista buscarPorNome(String nome) {
        return repositorio.findAll().stream()
                .filter(l -> l.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }
}

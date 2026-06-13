package com.gestaocompras.service;

import com.gestaocompras.model.Item;
import com.gestaocompras.model.Item.Unidade;
import com.gestaocompras.model.ItemComprado;
import com.gestaocompras.model.Lista;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repositório responsável por persistir e recuperar as listas de compras
 * em arquivo de texto no formato CSV simples (sem dependências externas).
 *
 * Formato do arquivo:
 *   LISTA:<nome>
 *   ITEM:<descricao>|<unidade>|<quantidade>
 *   COMPRADO:<descricao>|<unidade>|<qtdComprada>|<precoUnitario>
 *   END
 */
public class ListaRepository {

    private static final String ARQUIVO_DADOS = "listas.dat";

    public List<Lista> findAll() {
        return carregarDados();
    }

    public boolean existeNome(String nome) {
        return carregarDados().stream().anyMatch(l -> l.getNome().equalsIgnoreCase(nome));
    }

    public void salvar(Lista lista) {
        List<Lista> todas = carregarDados();
        todas.removeIf(l -> l.getNome().equalsIgnoreCase(lista.getNome()));
        todas.add(lista);
        persistirDados(todas);
    }

    private List<Lista> carregarDados() {
        Path path = Paths.get(ARQUIVO_DADOS);
        if (!Files.exists(path)) return new ArrayList<>();

        List<Lista> resultado = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARQUIVO_DADOS))) {
            String linha;
            Lista listaAtual = null;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.startsWith("LISTA:")) {
                    listaAtual = new Lista(linha.substring(6));
                } else if (linha.startsWith("ITEM:") && listaAtual != null) {
                    String[] p = linha.substring(5).split("\\|", -1);
                    if (p.length == 3) {
                        String desc = p[0];
                        Unidade un = Unidade.fromString(p[1]);
                        double qtd = Double.parseDouble(p[2].replace(',', '.'));
                        listaAtual.adicionarItem(new Item(desc, un, qtd));
                    }
                } else if (linha.startsWith("COMPRADO:") && listaAtual != null) {
                    String[] p = linha.substring(9).split("\\|", -1);
                    if (p.length == 4) {
                        String desc = p[0];
                        Unidade un = Unidade.fromString(p[1]);
                        double qtdC = Double.parseDouble(p[2].replace(',', '.'));
                        double preco = Double.parseDouble(p[3].replace(',', '.'));
                        // Localiza o item correspondente
                        Item item = listaAtual.getItens().stream()
                                .filter(it -> it.getDescricao().equals(desc))
                                .findFirst()
                                .orElse(new Item(desc, un, qtdC));
                        listaAtual.getItensComprados().add(new ItemComprado(item, qtdC, preco));
                    }
                } else if (linha.equals("END") && listaAtual != null) {
                    if (!listaAtual.getItensComprados().isEmpty()) {
                        listaAtual.setCompraRealizada(true);
                    }
                    resultado.add(listaAtual);
                    listaAtual = null;
                }
            }
        } catch (IOException e) {
            System.err.println("Aviso: não foi possível carregar dados: " + e.getMessage());
        }
        return resultado;
    }

    private void persistirDados(List<Lista> listas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO_DADOS))) {
            for (Lista lista : listas) {
                pw.println("LISTA:" + lista.getNome());
                for (Item item : lista.getItens()) {
                    pw.printf(Locale.US, "ITEM:%s|%s|%.4f%n",
                            item.getDescricao(), item.getUnidade(), item.getQuantidade());
                }
                for (ItemComprado ic : lista.getItensComprados()) {
                    pw.printf(Locale.US, "COMPRADO:%s|%s|%.4f|%.4f%n",
                            ic.getItem().getDescricao(),
                            ic.getItem().getUnidade(),
                            ic.getQuantidadeComprada(),
                            ic.getPrecoUnitario());
                }
                pw.println("END");
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }
    }
}

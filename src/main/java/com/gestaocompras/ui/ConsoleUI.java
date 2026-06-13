package com.gestaocompras.ui;

import com.gestaocompras.model.Item;
import com.gestaocompras.model.Item.Unidade;
import com.gestaocompras.model.ItemComprado;
import com.gestaocompras.model.Lista;
import com.gestaocompras.service.ListaService;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interface de usuário via console para o sistema de Gestão de Compras.
 */
public class ConsoleUI {

    private final Scanner scanner;
    private final ListaService service;

    public ConsoleUI() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
        this.scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        this.service = new ListaService();
    }

    public void iniciar() {
        boolean executando = true;
        while (executando) {
            exibirMenuPrincipal();
            String opcao = lerLinha(">> Opção");
            switch (opcao.trim()) {
                case "1":
                    novaLista();
                    break;
                case "2":
                    fazerCompras();
                    break;
                case "3":
                    relatorio();
                    break;
                case "0":
                    executando = false;
                    System.out.println("\nAté logo!");
                    break;
                default:
                    System.out.println("\nOpção inválida. Tente novamente.");
            }
        }
    }

    // ─────────────────────────────────────────────
    //  MENU PRINCIPAL
    // ─────────────────────────────────────────────

    private void exibirMenuPrincipal() {
        System.out.println();
        System.out.println(".-------------------.");
        System.out.println("| Gestão de compras |");
        System.out.println("'-------------------'");
        System.out.println("Selecione a opção:");
        System.out.println("1. Nova lista");
        System.out.println("2. Fazer compras");
        System.out.println("3. Relatório");
        System.out.println("0. Sair");
    }

    // ─────────────────────────────────────────────
    //  NOVA LISTA
    // ─────────────────────────────────────────────

    private void novaLista() {
        String nomePadrao = gerarNomePadrao();
        System.out.println();
        System.out.print(">> Nova lista, informe o nome [" + nomePadrao + "]: ");
        String entrada = scanner.nextLine().trim();
        String nome = entrada.isEmpty() ? nomePadrao : entrada;

        // Validar unicidade
        if (service.buscarPorNome(nome) != null) {
            System.out.println(">> Já existe uma lista com o nome \"" + nome + "\". Escolha outro nome.");
            return;
        }

        Lista lista = service.criarLista(nome);

        // Adicionar itens
        while (true) {
            System.out.println(">> ---Informe o item---------");
            String descricao = lerLinha(">> Descrição");
            if (descricao.isEmpty()) {
                break;
            }

            Unidade unidade = lerUnidade();
            double quantidade = lerDouble(">> Quantidade");

            Item item = new Item(descricao, unidade, quantidade);
            service.adicionarItem(lista, item);
        }

        service.salvarLista(lista);
        System.out.println(">> ---Lista salva!---------");
        if (lista.temItens()) {
            System.out.println(">> " + lista.getItens().size() + " item(s) na lista \"" + lista.getNome() + "\"");
        }
    }

    private String gerarNomePadrao() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        return "lista_" + LocalDate.now().format(fmt);
    }

    private Unidade lerUnidade() {
        while (true) {
            System.out.print(">> Unidade (UN, CX, KG, LT): ");
            String entrada = scanner.nextLine().replace("\r", "").trim().toUpperCase();
            Unidade unidade = Unidade.fromString(entrada);
            if (unidade != null) {
                return unidade;
            }
            System.out.println(">> Unidade inválida. Use: UN, CX, KG ou LT");
        }
    }

    // ─────────────────────────────────────────────
    //  FAZER COMPRAS
    // ─────────────────────────────────────────────

    private void fazerCompras() {
        List<Lista> listas = service.listarComItens();
        if (listas.isEmpty()) {
            System.out.println("\n>> Nenhuma lista disponível. Crie uma lista primeiro.");
            return;
        }

        Lista lista = selecionarLista(listas, "Fazer compras");
        if (lista == null) return;

        System.out.println(">> ---Fazer compras [" + lista.getNome() + "]---");

        List<Item> itens = lista.getItens();
        int total = itens.size();
        List<ItemComprado> itensComprados = new ArrayList<>();

        // Carregar compra anterior (se houver) para usar como padrão
        List<ItemComprado> compraAnterior = lista.getItensComprados();

        for (int i = 0; i < total; i++) {
            Item item = itens.get(i);
            ItemComprado anterior = encontrarCompraAnterior(compraAnterior, item);

            double qtdPadrao = anterior != null ? anterior.getQuantidadeComprada() : item.getQuantidade();

            System.out.printf("%n>> (%d/%d) Produto: %s%n", (i + 1), total, item.getDescricao());
            System.out.printf(">> Quantidade planejada: %.2f %s%n", item.getQuantidade(), item.getUnidade());

            // Quantidade de compra
            double quantidade = lerDoubleComPadrao(
                    String.format(">> Quantidade [%.2f %s]", qtdPadrao, item.getUnidade()),
                    qtdPadrao
            );

            // Preço
            double preco = lerDouble(">> Preço");

            if (preco == 0.0) {
                System.out.println(">> Item em falta — pulando para o próximo.");
                ItemComprado ic = new ItemComprado(item, 0, 0);
                itensComprados.add(ic);
                continue;
            }

            ItemComprado ic = new ItemComprado(item, quantidade, preco);
            itensComprados.add(ic);
            System.out.printf(">> Total do item: R$ %.2f%n", ic.getTotalItem());
        }

        service.registrarCompra(lista, itensComprados);

        System.out.println("\n>> ---Total------------------");
        System.out.printf(">> R$: %.2f%n", lista.getTotalCompra());
    }

    private ItemComprado encontrarCompraAnterior(List<ItemComprado> compraAnterior, Item item) {
        if (compraAnterior == null) return null;
        return compraAnterior.stream()
                .filter(ic -> ic.getItem().getDescricao().equalsIgnoreCase(item.getDescricao()))
                .findFirst()
                .orElse(null);
    }

    // ─────────────────────────────────────────────
    //  RELATÓRIO
    // ─────────────────────────────────────────────

    private void relatorio() {
        List<Lista> listas = service.listarComCompraRealizada();
        if (listas.isEmpty()) {
            System.out.println("\n>> Nenhuma compra realizada ainda.");
            return;
        }

        Lista lista = selecionarLista(listas, "Relatório");
        if (lista == null) return;

        List<ItemComprado> itens = lista.getItensComprados();

        System.out.println(">> ---Relatório [" + lista.getNome() + "]---");
        System.out.println(">> Item, Descrição, Qtd, UN, Preço Unit., Total");

        double totalQtd = 0;
        double totalGeral = 0;

        for (int i = 0; i < itens.size(); i++) {
            ItemComprado ic = itens.get(i);
            Item item = ic.getItem();

            if (ic.estaEmFalta()) {
                System.out.printf(">> %d, %s, -, %s, EM FALTA%n",
                        (i + 1),
                        item.getDescricao(),
                        item.getUnidade());
            } else {
                totalQtd += ic.getQuantidadeComprada();
                totalGeral += ic.getTotalItem();

                System.out.printf(">> %d, %s, %.2f, %s, R$ %.2f, R$ %.2f%n",
                        (i + 1),
                        item.getDescricao(),
                        ic.getQuantidadeComprada(),
                        item.getUnidade(),
                        ic.getPrecoUnitario(),
                        ic.getTotalItem());
            }
        }

        System.out.printf(">> 0, TOTAL, %.2f, UN, R$ %.2f%n", totalQtd, totalGeral);
    }

    // ─────────────────────────────────────────────
    //  UTILITÁRIOS DE SELEÇÃO E LEITURA
    // ─────────────────────────────────────────────

    private Lista selecionarLista(List<Lista> listas, String contexto) {
        System.out.println("\n>> Listas disponíveis:");
        for (int i = 0; i < listas.size(); i++) {
            System.out.printf("   %d. %s%n", (i + 1), listas.get(i).getNome());
        }

        while (true) {
            String entrada = lerLinha(">> Selecione a lista (número)");
            try {
                int indice = Integer.parseInt(entrada.trim()) - 1;
                if (indice >= 0 && indice < listas.size()) {
                    return listas.get(indice);
                }
            } catch (NumberFormatException ignored) {
                // Se o usuário digitou o nome diretamente
                for (Lista l : listas) {
                    if (l.getNome().equalsIgnoreCase(entrada.trim())) {
                        return l;
                    }
                }
            }
            System.out.println(">> Seleção inválida. Tente novamente.");
        }
    }

    private String lerLinha(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().replace("\r", "").trim();
    }

    private double lerDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String entrada = scanner.nextLine().replace("\r", "").trim().replace(",", ".");
            try {
                double valor = Double.parseDouble(entrada);
                if (valor < 0) {
                    System.out.println(">> Valor não pode ser negativo.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println(">> Valor inválido. Informe um número (ex: 5 ou 29.90).");
            }
        }
    }

    private double lerDoubleComPadrao(String prompt, double padrao) {
        while (true) {
            System.out.print(prompt + ": ");
            String entrada = scanner.nextLine().replace("\r", "").trim().replace(",", ".");
            if (entrada.isEmpty()) {
                return padrao;
            }
            try {
                double valor = Double.parseDouble(entrada);
                if (valor < 0) {
                    System.out.println(">> Valor não pode ser negativo.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println(">> Valor inválido. Pressione Enter para usar o padrão ou informe um número.");
            }
        }
    }
}

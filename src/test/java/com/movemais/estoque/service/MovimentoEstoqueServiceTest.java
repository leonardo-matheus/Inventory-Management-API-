package com.movemais.estoque.service;

import com.movemais.estoque.dto.movimento.MovimentoCreateRequest;
import com.movemais.estoque.entity.*;
import com.movemais.estoque.exception.BusinessException;
import com.movemais.estoque.exception.NotFoundException;
import com.movemais.estoque.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovimentoEstoqueServiceTest {

    private final ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
    private final DepositoRepository depositoRepository = mock(DepositoRepository.class);
    private final EstoqueRepository estoqueRepository = mock(EstoqueRepository.class);
    private final MovimentoEstoqueRepository movimentoRepository = mock(MovimentoEstoqueRepository.class);

    private final MovimentoEstoqueService service =
            new MovimentoEstoqueService(produtoRepository, depositoRepository, estoqueRepository, movimentoRepository);

    @Test
    void deveRegistrarEntradaSomandoSaldo() {
        Produto produto = Produto.builder()
                .id(1L).sku("SKU-1").nome("P1")
                .precoUnitario(BigDecimal.TEN).ativo(true).build();

        Deposito deposito = Deposito.builder()
                .id(1L).codigo("D1").nome("Dep").build();

        Estoque estoque = Estoque.builder()
                .id(1L).produto(produto).deposito(deposito)
                .quantidadeAtual(10L).build();

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(estoqueRepository.findByProdutoAndDeposito(produto, deposito)).thenReturn(Optional.of(estoque));

        // mocka o save para NÃO retornar null
        when(movimentoRepository.save(any(MovimentoEstoque.class)))
                .thenAnswer(invocation -> {
                    MovimentoEstoque mov = invocation.getArgument(0);
                    mov.setId(100L);
                    if (mov.getDataHoraMovimento() == null) {
                        mov.setDataHoraMovimento(OffsetDateTime.now());
                    }
                    return mov;
                });

        MovimentoCreateRequest req = new MovimentoCreateRequest(1L, 1L, 5L, "entrada de teste");

        var resp = service.registrarEntrada(req);

        // saldo atualizado em memória
        assertEquals(15L, estoque.getQuantidadeAtual());
        verify(estoqueRepository).save(estoque);
        verify(movimentoRepository).save(Mockito.any(MovimentoEstoque.class));

        // MovimentoResponse é RECORD: usar acessores do record
        assertEquals(5L, resp.quantidade());
        assertEquals("ENTRADA", resp.tipoMovimento());
        assertEquals(1L, resp.produtoId());
        assertEquals(1L, resp.depositoId());
        assertNotNull(resp.dataHoraMovimento());
    }

    @Test
    void deveFalharSaidaSemSaldo() {
        Produto produto = Produto.builder().id(1L).sku("SKU-1").nome("P1")
                .precoUnitario(BigDecimal.TEN).ativo(true).build();
        Deposito deposito = Deposito.builder().id(1L).codigo("D1").nome("Dep").build();
        Estoque estoque = Estoque.builder().id(1L).produto(produto).deposito(deposito).quantidadeAtual(2L).build();

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(estoqueRepository.findByProdutoAndDeposito(produto, deposito)).thenReturn(Optional.of(estoque));

        MovimentoCreateRequest req = new MovimentoCreateRequest(1L, 1L, 5L, "saida sem saldo");

        assertThrows(BusinessException.class, () -> service.registrarSaida(req));
    }

    @Test
    void deveFalharQuandoProdutoNaoExiste() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());
        MovimentoCreateRequest req = new MovimentoCreateRequest(1L, 1L, 1L, null);
        assertThrows(NotFoundException.class, () -> service.registrarEntrada(req));
    }
}
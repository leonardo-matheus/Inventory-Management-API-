package com.movemais.estoque.service;

import com.movemais.estoque.dto.movimento.MovimentoCreateRequest;
import com.movemais.estoque.dto.movimento.MovimentoResponse;
import com.movemais.estoque.entity.*;
import com.movemais.estoque.exception.BusinessException;
import com.movemais.estoque.exception.NotFoundException;
import com.movemais.estoque.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class MovimentoEstoqueService {

    private final ProdutoRepository produtoRepository;
    private final DepositoRepository depositoRepository;
    private final EstoqueRepository estoqueRepository;
    private final MovimentoEstoqueRepository movimentoRepository;

    @Transactional
    public MovimentoResponse registrarEntrada(MovimentoCreateRequest request) {
        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));

        Deposito deposito = depositoRepository.findById(request.depositoId())
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado"));

        Estoque estoque = estoqueRepository.findByProdutoAndDeposito(produto, deposito)
                .orElseGet(() -> Estoque.builder()
                        .produto(produto)
                        .deposito(deposito)
                        .quantidadeAtual(0L)
                        .build());

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() + request.quantidade());
        estoqueRepository.save(estoque);

        MovimentoEstoque movimento = MovimentoEstoque.builder()
                .tipoMovimento(MovimentoEstoque.TipoMovimento.ENTRADA)
                .produto(produto)
                .deposito(deposito)
                .quantidade(request.quantidade())
                .dataHoraMovimento(OffsetDateTime.now())
                .observacao(request.observacao())
                .usuarioResponsavel("admin") // ou obter do SecurityContext
                .build();

        MovimentoEstoque salvo = movimentoRepository.save(movimento);

        return toResponse(salvo);
    }

    @Transactional
    public MovimentoResponse registrarSaida(MovimentoCreateRequest request) {
        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));

        Deposito deposito = depositoRepository.findById(request.depositoId())
                .orElseThrow(() -> new NotFoundException("Depósito não encontrado"));

        Estoque estoque = estoqueRepository.findByProdutoAndDeposito(produto, deposito)
                .orElseThrow(() -> new BusinessException("Não há estoque para este produto no depósito informado"));

        if (estoque.getQuantidadeAtual() < request.quantidade()) {
            throw new BusinessException("Saldo insuficiente para saída de estoque");
        }

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() - request.quantidade());
        estoqueRepository.save(estoque);

        MovimentoEstoque movimento = MovimentoEstoque.builder()
                .tipoMovimento(MovimentoEstoque.TipoMovimento.SAIDA)
                .produto(produto)
                .deposito(deposito)
                .quantidade(request.quantidade())
                .dataHoraMovimento(OffsetDateTime.now())
                .observacao(request.observacao())
                .usuarioResponsavel("admin")
                .build();

        MovimentoEstoque salvo = movimentoRepository.save(movimento);

        return toResponse(salvo);
    }

    public Page<MovimentoResponse> listar(Pageable pageable) {
        return movimentoRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private MovimentoResponse toResponse(MovimentoEstoque m) {
        return new MovimentoResponse(
                m.getId(),
                m.getTipoMovimento().name(),
                m.getProduto().getId(),
                m.getDeposito().getId(),
                m.getQuantidade(),
                m.getDataHoraMovimento(),
                m.getObservacao(),
                m.getUsuarioResponsavel()
        );
    }
}
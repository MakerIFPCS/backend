package com.maker.website.solicitacoes;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<SolicitacaoEntity, Long> {
    List<SolicitacaoEntity> findAllByTypeOrderByCreatedAtDesc(String type);
}

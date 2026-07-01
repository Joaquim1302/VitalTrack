package com.app.vitaltrack.repository.treinos

import com.app.vitaltrack.data.entity.treinos.TreinoSessaoEntity

sealed class TreinoSessaoResult {
    data class SessaoCriada(val sessao: TreinoSessaoEntity) : TreinoSessaoResult()
    data class SessaoRetomada(val sessao: TreinoSessaoEntity) : TreinoSessaoResult()
    data class SessaoEmAndamentoDeOutroTreino(val sessao: TreinoSessaoEntity) : TreinoSessaoResult()
    data class Erro(val mensagem: String) : TreinoSessaoResult()
}

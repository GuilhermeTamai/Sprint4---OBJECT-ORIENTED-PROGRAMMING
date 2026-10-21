-- =========================================================
-- SCRIPT DE INSERÇÃO DE DADOS - SPRINT 3
-- =========================================================
 
-- Inserindo as Equipes
INSERT INTO TB_EQUIPE_MANUTENCAO (nome_equipe) VALUES ('Química-Beta');
INSERT INTO TB_EQUIPE_MANUTENCAO (nome_equipe) VALUES ('Tracto-Alpha');
 
-- Inserindo os Trechos iniciais
INSERT INTO TB_TRECHO_RODOVIA VALUES ('Anhanguera-Norte', 50.0, 60.0, 25.0, 0, 0);
INSERT INTO TB_TRECHO_RODOVIA VALUES ('Bandeirantes-Sul', 20.0, 35.0, 10.0, 1, 0);
INSERT INTO TB_TRECHO_RODOVIA VALUES ('Dutra-IoT-Serra', 100.0, 115.0, 35.0, 1, 1);
 
-- Inserindo uma intervenção de teste
INSERT INTO TB_INTERVENCAO_OPERACIONAL (tipo_intervencao, id_equipe, id_trecho)
VALUES (
    'PULVERIZACAO', 
    (SELECT id_equipe FROM TB_EQUIPE_MANUTENCAO WHERE nome_equipe = 'Química-Beta'), 
    'Anhanguera-Norte'
);
 
-- Inserindo um relatório inicial de teste
INSERT INTO TB_RELATORIO_PRIORIDADE (qt_urgente, qt_critico, qt_atencao, qt_normal, resumo) 
VALUES (0, 1, 1, 1, 'Simulação inicial de teste do sistema.');
 
COMMIT;
INSERT INTO machines (
    name, code, description, status, access, type, image, created_at, updated_at
)
VALUES
    ('Impressora 3D FDM', 'DEMO-M001', 'Impressora para prototipagem de pecas em PLA.', 'AVAILABLE', 'RESTRICTED', 'Impressao 3D', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Cortadora a laser', 'DEMO-M002', 'Equipamento para corte e gravacao a laser.', 'FUNCTIONAL', 'RESTRICTED', 'Corte a laser', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Impressora 3D de resina', 'DEMO-M003', 'Impressora de resina para pecas com alto nivel de detalhe.', 'AVAILABLE', 'RESTRICTED', 'Impressao 3D', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Router CNC', 'DEMO-M004', 'Router CNC para usinagem de placas e pecas.', 'MAINTENANCE', 'RESTRICTED', 'CNC', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Scanner 3D', 'DEMO-M005', 'Scanner para digitalizacao de objetos e prototipos.', 'AVAILABLE', 'FREE', 'Digitalizacao 3D', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Estacao de solda', 'DEMO-M006', 'Estacao de solda para montagem de prototipos eletronicos.', 'FUNCTIONAL', 'RESTRICTED', 'Eletronica', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Plotter de recorte', 'DEMO-M007', 'Plotter para recorte de vinil e materiais flexiveis.', 'UNUSABLE', 'RESTRICTED', 'Recorte', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

INSERT INTO agendamentos (
    code, requester_name, requester_type, course, email, registration_number, request_date,
    day_of_week, start_time, end_time, purpose, reason, description, equipment, status, created_at
)
SELECT seed.*, CURRENT_TIMESTAMP
FROM (VALUES
    ('DEMO-AG001', 'Ana Exemplo', 'ALUNO', 'Engenharia', 'ana.demo@example.invalid', 'DEMO-001', '2030-01-15', 'Terça-feira', '10:00', '11:00', 'Prototipagem de uma peca', 'Solicitacao ficticia para demonstracao.', 'Registro sem dados pessoais reais.', 'Impressora 3D FDM', 'Aguardando análise'),
    ('DEMO-AG002', 'Bruno Exemplo', 'ALUNO', 'Design', 'bruno.demo@example.invalid', 'DEMO-002', '2030-01-16', 'Quarta-feira', '13:00', '14:30', 'Criacao de modelo para projeto', 'Reserva de exemplo para projeto academico.', 'Modelo conceitual para atividade de aula.', 'Impressora 3D de resina', 'Aprovado'),
    ('DEMO-AG003', 'Carla Exemplo', 'SERVIDOR', 'Pesquisa', 'carla.demo@example.invalid', 'DEMO-003', '2030-01-17', 'Quinta-feira', '09:00', '10:00', 'Gravacao de placas', 'Reserva ficticia para demonstrar um pedido recusado.', 'Exemplo de solicitacao finalizada.', 'Cortadora a laser', 'Recusado'),
    ('DEMO-AG004', 'Diego Exemplo', 'ALUNO', 'Computacao', 'diego.demo@example.invalid', 'DEMO-004', '2030-01-18', 'Sexta-feira', '15:00', '16:00', 'Digitalizacao de uma peca', 'Reserva de exemplo para digitalizacao 3D.', 'Peca cenografica sem valor comercial.', 'Scanner 3D', 'Aguardando análise'),
    ('DEMO-AG005', 'Eva Exemplo', 'COMUNIDADE_EXTERNA', 'Oficina aberta', 'eva.demo@example.invalid', 'DEMO-005', '2030-01-19', 'Sábado', '11:00', '12:00', 'Corte de material para oficina', 'Reserva ficticia para atividade aberta.', 'Material de demonstracao fornecido pelo laboratorio.', 'Cortadora a laser', 'Aprovado'),
    ('DEMO-AG006', 'Felipe Exemplo', 'ALUNO', 'Eletronica', 'felipe.demo@example.invalid', 'DEMO-006', '2030-01-20', 'Domingo', '14:00', '15:00', 'Montagem de circuito', 'Reserva ficticia para uso da bancada de eletronica.', 'Atividade demonstrativa sem dados pessoais reais.', 'Estacao de solda', 'Aguardando análise')
) AS seed(code, requester_name, requester_type, course, email, registration_number, request_date, day_of_week, start_time, end_time, purpose, reason, description, equipment, status)
WHERE NOT EXISTS (SELECT 1 FROM agendamentos existing WHERE existing.code = seed.code);
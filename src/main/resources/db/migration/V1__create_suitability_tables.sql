-- V1__create_suitability_tables.sql
-- Suitability Engine Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Investors table
CREATE TABLE investors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name VARCHAR(255) NOT NULL,
    cpf_hash VARCHAR(64) NOT NULL UNIQUE,
    email_hash VARCHAR(64),
    profile VARCHAR(20) NOT NULL DEFAULT 'CONSERVADOR',
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    profile_assessed_at TIMESTAMP WITH TIME ZONE,
    profile_expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_investors_cpf_hash ON investors(cpf_hash);
CREATE INDEX idx_investors_profile ON investors(profile);
CREATE INDEX idx_investors_kyc_status ON investors(kyc_status);

-- Investment Products table
CREATE TABLE investment_products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    min_application NUMERIC(19,2) NOT NULL,
    max_application NUMERIC(19,2),
    liquidation_days INTEGER NOT NULL,
    has_performance_fee BOOLEAN NOT NULL DEFAULT FALSE,
    management_fee_bps NUMERIC(10,2) NOT NULL DEFAULT 0,
    performance_fee_bps NUMERIC(10,2) NOT NULL DEFAULT 0,
    benchmark VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_code ON investment_products(code);
CREATE INDEX idx_products_type ON investment_products(type);
CREATE INDEX idx_products_risk_level ON investment_products(risk_level);
CREATE INDEX idx_products_active ON investment_products(is_active);

-- Suitability Assessments table
CREATE TABLE suitability_assessment (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    investor_id UUID NOT NULL REFERENCES investors(id),
    product_id UUID NOT NULL REFERENCES investment_products(id),
    investor_profile VARCHAR(20) NOT NULL,
    product_risk VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    compatible BOOLEAN,
    violation_code VARCHAR(100),
    document_hash VARCHAR(500),
    reasoning TEXT,
    conditions JSONB,
    requires_document_signature BOOLEAN DEFAULT FALSE,
    document_hash VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_assessment_investor ON suitability_assessment(investor_id);
CREATE INDEX idx_assessment_product ON suitability_assessment(product_id);
CREATE INDEX idx_assessment_status ON suitability_assessment(status);
CREATE INDEX idx_assessment_idempotency ON suitability_assessment(idempotency_key);
CREATE INDEX idx_assessment_expires ON suitability_assessment(expires_at);
CREATE INDEX idx_assessment_investor_status ON suitability_assessment(investor_id, status);

-- Event Store table (Event Sourcing)
CREATE TABLE event_store (
    event_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    correlation_id UUID,
    causation_id UUID,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    hash_chain VARCHAR(64) NOT NULL,
    
    CONSTRAINT chk_event_type CHECK (event_type IN (
        'SuitabilityAssessmentCreated',
        'SuitabilityAssessed',
        'SuitabilityDocumentSigned',
        'SuitabilityAssessmentApproved',
        'SuitabilityAssessmentRejected',
        'SuitabilityAssessmentExpired',
        'SuitabilityAssessmentCancelled'
    ))
);

CREATE INDEX idx_event_store_aggregate ON event_store(aggregate_id);
CREATE INDEX idx_event_store_correlation ON event_store(correlation_id);
CREATE INDEX idx_event_store_timestamp ON event_store(timestamp);
CREATE INDEX idx_event_store_type ON event_store(event_type);

-- Outbox Pattern table
CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    topic VARCHAR(200) NOT NULL,
    key VARCHAR(100) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP WITH TIME ZONE,
    last_error TEXT
);

CREATE INDEX idx_outbox_unpublished ON outbox_event(published, created_at) WHERE published = FALSE;
CREATE INDEX idx_outbox_aggregate ON outbox_event(aggregate_id);

-- Dead Letter Queue
CREATE TABLE dead_letter_event (
    id BIGSERIAL PRIMARY KEY,
    original_event_id BIGINT NOT NULL,
    topic VARCHAR(200) NOT NULL,
    payload JSONB NOT NULL,
    error TEXT NOT NULL,
    retry_count INTEGER NOT NULL,
    failed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_assessment_investor_status ON suitability_assessment(investor_id, status);
CREATE INDEX idx_assessment_product_status ON suitability_assessment(product_id, status);
CREATE INDEX idx_assessment_created ON suitability_assessment(created_at);

-- Comments
COMMENT ON TABLE investors IS 'Investidores com perfil de suitability (CVM 558)';
COMMENT ON TABLE investment_products IS 'Produtos de investimento com classificação de risco';
COMMENT ON TABLE suitability_assessment IS 'Avaliações de suitability (CVM 558)';
COMMENT ON TABLE event_store IS 'Event Store para Event Sourcing (auditoria imutável)';
COMMENT ON TABLE outbox_event IS 'Outbox Pattern para publicação garantida de eventos';
COMMENT ON TABLE dead_letter_event IS 'Dead Letter Queue para eventos falhos';
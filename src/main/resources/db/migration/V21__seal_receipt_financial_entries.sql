-- Receipt issuance assembles the ledger before binding the documentary identity.
-- Once bound, no further allocations or components may extend that settlement,
-- even within its creating transaction or after deferred checks were consumed.
-- V18 continues to govern generic operations; V20 validates the initial binding.
CREATE FUNCTION financial_receipt_entry_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_receipt WHERE operation_id=NEW.operation_id)
 THEN RAISE EXCEPTION 'Cannot append to a receipt-bound financial operation'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_receipt_allocation_sealed BEFORE INSERT ON financial_allocation
 FOR EACH ROW EXECUTE FUNCTION financial_receipt_entry_guard();
CREATE TRIGGER financial_receipt_component_sealed BEFORE INSERT ON financial_component
 FOR EACH ROW EXECUTE FUNCTION financial_receipt_entry_guard();

package com.ar2lda.fac.financial;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static com.ar2lda.fac.financial.FinancialCore.*;

/** Internal reads only. Scope must be resolved by an authorised caller, never trusted from a public payload. */
public interface FinancialQueries {
    enum Selection { ALL, OPEN, CLOSED }
    enum State { OPEN, PARTIAL, EXHAUSTED, CANCELLED }
    enum OperationKind { CREATE, SETTLE, REVERSE }
    enum Effect { CONSUME, RESTORE, CANCEL }
    enum ComponentKind { CASH_IN, CASH_OUT, DISCOUNT }
    enum DocumentLinkStatus { NOT_RECORDED, LINKED }

    record Page<T>(List<T> items, long total, int offset, int limit) {
        public Page { items = List.copyOf(items); }
    }
    record CommercialOrigin(long documentId, String reference, LocalDate issueDate, LocalDate dueDate) {}
    record FinancialDocument(long documentId, String reference, LocalDate issueDate) {}
    record PositionView(long id, Scope scope, Nature nature, String origin, String reference,
                        BigDecimal original, BigDecimal residual, State state, long creationOperationId,
                        Optional<CommercialOrigin> commercialOrigin, Instant recordedAt) {
        /** TUULI-FIN-TIME-001: origin effect date is derived from its document, never recordedAt. */
        public Optional<LocalDate> financialEffectDate() { return commercialOrigin.map(CommercialOrigin::issueDate); }
    }
    record Balance(Scope scope, BigDecimal openDebt, BigDecimal availableCredit, BigDecimal netDebt,
                   long openPositions, long closedPositions) {}
    record Allocation(long id, long positionId, long operationId, BigDecimal amount, Effect effect,
                      Long reverseOfAllocationId, OperationKind operationKind, Long reverseOfOperationId,
                      Instant recordedAt) {}
    record Component(long id, ComponentKind kind, BigDecimal amount, boolean reversal, Long reverseOfComponentId) {}
    /** Financial documents are identified only by a persisted link, never guessed from amount/client/date. */
    record OperationView(long id, Scope scope, OperationKind kind, String actor, String reason,
                         Instant recordedAt, Long reverseOfOperationId, Long reversedByOperationId,
                         Optional<CommercialOrigin> creationDocument, DocumentLinkStatus financialDocumentLink,
                         Optional<FinancialDocument> financialDocument,
                         List<Component> components) {
        public OperationView { components = List.copyOf(components); }
        public Optional<LocalDate> financialEffectDate() {
            return switch(kind) {
                case CREATE -> creationDocument.map(CommercialOrigin::issueDate);
                case SETTLE -> financialDocument.map(FinancialDocument::issueDate);
                case REVERSE -> Optional.empty(); // Reversal effect semantics have NOT been approved.
            };
        }
    }

    Page<PositionView> positions(Scope scope, Selection selection, Nature nature, int offset, int limit);
    PositionView position(Scope scope, long id);
    Optional<PositionView> byCommercialDocument(Scope scope, long documentId);
    Balance balance(Scope scope);
    Page<Allocation> allocations(Scope scope, long positionId, int offset, int limit);
    OperationView operation(Scope scope, long id);

    /** Documentary effect date, not recordedAt. Suspended until settlement links/reversal semantics exist. */
    Balance balanceAt(Scope scope, LocalDate financialDate);
}

package h207fixture;

import jakarta.persistence.*;
import java.util.UUID;

/** Explicitly registered by the isolated test; outside the application's entity scan. */
@Entity @Table(name="jpa_witness",schema="signature_trial")
public class TrialWitness {
    @Id public UUID id;
    public String name;
    public TrialWitness() { }
    public TrialWitness(UUID id) { this.id=id; this.name="rollback"; }
}

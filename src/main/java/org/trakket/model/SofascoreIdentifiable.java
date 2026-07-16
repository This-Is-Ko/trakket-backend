package org.trakket.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class SofascoreIdentifiable {

    @Column(name = "sofascore_external_id", unique = true)
    private Long sofascoreExternalId;

}

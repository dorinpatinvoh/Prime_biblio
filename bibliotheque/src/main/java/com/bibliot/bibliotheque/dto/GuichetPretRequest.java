package com.bibliot.bibliotheque.dto;

import lombok.Data;

@Data
public class GuichetPretRequest {
    private Long utilisateurId;
    private Long exemplaireId;
}

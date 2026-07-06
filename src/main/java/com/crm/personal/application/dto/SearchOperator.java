package com.crm.personal.application.dto;

public enum SearchOperator {
    /** El contacto debe tener TODAS las etiquetas del filtro. */
    AND,
    /** El contacto debe tener AL MENOS UNA de las etiquetas del filtro. */
    OR
}

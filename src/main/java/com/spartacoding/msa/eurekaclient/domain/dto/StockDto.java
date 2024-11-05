package com.spartacoding.msa.eurekaclient.domain.dto;

import java.io.Serializable;


public record StockDto(
    Long id,
   Integer stock
) implements Serializable {

}



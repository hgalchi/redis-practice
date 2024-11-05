package com.spartacoding.msa.eurekaclient.domain.dto;


import com.spartacoding.msa.eurekaclient.domain.Store;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto implements Serializable {
    private Long id;
    private String name;
    private String category;

    public static StoreDto fromEntity(Store entity) {
        return StoreDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .build();
    }
}

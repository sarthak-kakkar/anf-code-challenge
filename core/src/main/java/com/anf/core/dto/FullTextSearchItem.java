package com.anf.core.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FullTextSearchItem {

    private String title;
    private String description;
    private String image;
    private String lastModified;
}

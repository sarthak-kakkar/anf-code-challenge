package com.anf.core.models;

import lombok.Getter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class)
@Getter
public class AnFFormModel {

    @ValueMapValue
    private String inputlabel;

    @ValueMapValue
    private String buttontext;
}

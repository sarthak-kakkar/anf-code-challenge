package com.anf.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@XmlRootElement(name = "searchResponse")
public class SearchResponse {

    @XmlAttribute
    private String firstName;

    @XmlAttribute
    private String lastName;

    @XmlElementWrapper(name = "childPages")
    @XmlElement(name = "path")
    private List<String> childPages;
}

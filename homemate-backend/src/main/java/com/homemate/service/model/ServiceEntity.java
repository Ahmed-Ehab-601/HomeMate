package com.homemate.service.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

@Table("services") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEntity {

    @Id
    private Long id;

    private String name;
    private String description;
    private byte[] imageData;
    private String imageName;
    private String imageType;
}
